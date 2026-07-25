package com.portfolio.management.infrastructure.adapters.incoming.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.management.domain.port.incoming.RecordStockUsageUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.redis.dto.EventEnvelope;
import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.stream.ReactiveStreamCommands;
import io.quarkus.redis.datasource.stream.StreamMessage;
import io.quarkus.redis.datasource.stream.XReadGroupArgs;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.Context;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Consumes transaction:created from Redis Streams to feed the popularity
 * signal. Publisher-agnostic: reads its own consumer group regardless of
 * which service XADDs to the stream.
 * <p>
 * At-least-once, ack-on-handled: every message is acknowledged once a
 * result (success, ignored, error, or parse failure) is reached - there is
 * no DLQ or replay ladder, since a rare double-counted or dropped
 * transaction is immaterial to a ranking signal fed through log(1+count).
 */
@ApplicationScoped
public class TransactionCreatedConsumer {

    static final String STREAM_NAME = "transaction:created";

    private final RecordStockUsageUseCase recordStockUsageUseCase;
    private final RedisStreamConfig config;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisDataSource redisDataSource;

    private ReactiveStreamCommands<String, String, String> streamCommands;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean drainingPending = new AtomicBoolean(true);

    public TransactionCreatedConsumer(RecordStockUsageUseCase recordStockUsageUseCase,
                                      RedisStreamConfig config,
                                      ObjectMapper objectMapper,
                                      ReactiveRedisDataSource redisDataSource) {
        this.recordStockUsageUseCase = recordStockUsageUseCase;
        this.config = config;
        this.objectMapper = objectMapper;
        this.redisDataSource = redisDataSource;
    }

    @PostConstruct
    void init() {
        this.streamCommands = redisDataSource.stream(String.class, String.class, String.class);
    }

    public Cancellable startConsuming() {
        if (!running.compareAndSet(false, true)) {
            Log.warnf("Consumer for stream %s is already running", STREAM_NAME);
            return null;
        }

        Log.infof("Starting consumer for stream %s as %s in group %s",
                STREAM_NAME, config.consumerName(), config.group());

        return createPipeline()
                .subscribe().with(
                        messageId -> Log.infof("Processed message %s from stream %s", messageId, STREAM_NAME),
                        failure -> Log.errorf(failure, "Consumer pipeline for stream %s terminated", STREAM_NAME),
                        () -> Log.warnf("Consumer pipeline for stream %s completed unexpectedly", STREAM_NAME));
    }

    public void stop() {
        running.set(false);
    }

    private Multi<String> createPipeline() {
        return Multi.createBy().repeating()
                .uni(this::fetchMessages)
                .whilst(ignored -> running.get())
                .onItem().transformToMulti(messages -> Multi.createFrom().iterable(messages))
                .concatenate()
                .onItem().transformToUniAndConcatenate(this::processMessage);
    }

    private Uni<List<StreamMessage<String, String, String>>> fetchMessages() {
        // First drain this consumer's own pending entries (offset "0"), then switch
        // to live reads (">"); XAUTOCLAIM of other consumers' entries is out of scope
        // for a single-instance deployment.
        String offset = drainingPending.get() ? "0" : ">";
        Map<String, String> streamOffsets = Map.of(STREAM_NAME, offset);
        XReadGroupArgs args = new XReadGroupArgs()
                .count(config.readCount())
                .block(Duration.ofMillis(config.blockMs()));

        return streamCommands.xreadgroup(config.group(), config.consumerName(), streamOffsets, args)
                .onItem().invoke(messages -> {
                    if (drainingPending.get() && messages.isEmpty()) {
                        drainingPending.set(false);
                        Log.infof("Pending entries drained for stream %s, switching to live reads", STREAM_NAME);
                    }
                })
                .onFailure().invoke(throwable ->
                        Log.errorf(throwable, "Failed to read from stream %s", STREAM_NAME))
                .onFailure().recoverWithItem(List.of());
    }

    Uni<String> processMessage(StreamMessage<String, String, String> message) {
        String messageId = message.id();
        Map<String, String> fields = message.payload();

        // The use case writes through Panache's @WithTransaction, which requires a Vert.x
        // duplicated context.
        return Uni.createFrom().voidItem()
                .emitOn(duplicatedContextExecutor())
                .onItem().transformToUni(ignored -> parseCommand(fields))
                .onItem().transformToUni(recordStockUsageUseCase::execute)
                .onItem().invoke(result -> logResult(messageId, result))
                .onFailure().invoke(throwable ->
                        Log.warnf(throwable, "Skipping message %s on stream %s", messageId, STREAM_NAME))
                .onFailure().recoverWithItem((RecordStockUsageUseCase.Result) null)
                .onItem().transformToUni(ignored -> acknowledge(messageId));
    }

    private static Executor duplicatedContextExecutor() {
        Context context = VertxContext.getOrCreateDuplicatedContext();
        return context == null
                ? Runnable::run
                : action -> context.runOnContext(ignored -> action.run());
    }

    private Uni<RecordStockUsageUseCase.Command> parseCommand(Map<String, String> fields) {
        return Uni.createFrom().item(() -> {
            String payload = fields.get("payload");
            if (payload == null) {
                throw new IllegalArgumentException("Missing 'payload' field in stream message");
            }

            EventEnvelope envelope;
            try {
                envelope = objectMapper.readValue(payload, EventEnvelope.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to deserialize event envelope", e);
            }

            var data = envelope.payload();
            if (data == null || data.ticker() == null || data.ticker().isBlank()) {
                throw new IllegalArgumentException("Missing ticker in transaction created payload");
            }

            return new RecordStockUsageUseCase.Command(data.ticker(), data.exchange(), data.currency());
        });
    }

    private void logResult(String messageId, RecordStockUsageUseCase.Result result) {
        switch (result) {
            case RecordStockUsageUseCase.Result.Success ignored ->
                    Log.debugf("Recorded stock usage for message %s", messageId);
            case RecordStockUsageUseCase.Result.Ignored ignored ->
                    Log.debugf("Ignored message %s: %s", messageId, ignored.reason());
            case RecordStockUsageUseCase.Result.Error error ->
                    Log.warnf("Failed to record stock usage for message %s: %s", messageId, error.message());
        }
    }

    private Uni<String> acknowledge(String messageId) {
        return streamCommands.xack(STREAM_NAME, config.group(), messageId)
                .onItem().transform(count -> messageId)
                .onFailure().invoke(throwable ->
                        Log.errorf(throwable, "Failed to acknowledge message %s on stream %s", messageId, STREAM_NAME))
                .onFailure().recoverWithItem(messageId);
    }
}
