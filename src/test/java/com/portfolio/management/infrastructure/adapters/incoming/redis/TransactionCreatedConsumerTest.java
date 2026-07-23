package com.portfolio.management.infrastructure.adapters.incoming.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.management.domain.port.incoming.RecordStockUsageUseCase;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.stream.ReactiveStreamCommands;
import io.quarkus.redis.datasource.stream.StreamMessage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests processMessage directly (package-private) rather than driving the
 * full infinite XREADGROUP polling pipeline, matching the mocking style of
 * transactions-api's RedisPublisherTest.
 */
class TransactionCreatedConsumerTest {

    private static final String STREAM = "transaction:created";
    private static final String GROUP = "suggestions-consumers";

    private ReactiveStreamCommands<String, String, String> streamCommands;
    private RecordStockUsageUseCase recordStockUsageUseCase;
    private TransactionCreatedConsumer consumer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ReactiveRedisDataSource redisDataSource = mock(ReactiveRedisDataSource.class);
        streamCommands = (ReactiveStreamCommands<String, String, String>) mock(ReactiveStreamCommands.class);
        recordStockUsageUseCase = mock(RecordStockUsageUseCase.class);
        RedisStreamConfig config = mock(RedisStreamConfig.class);

        when(redisDataSource.stream(String.class, String.class, String.class)).thenReturn(streamCommands);
        when(config.group()).thenReturn(GROUP);
        when(streamCommands.xack(anyString(), anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(1));

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        consumer = new TransactionCreatedConsumer(recordStockUsageUseCase, config, objectMapper, redisDataSource);
        consumer.init();
    }

    @Test
    @DisplayName("Should parse a valid message, record usage, and acknowledge it")
    void shouldProcessValidMessage() {
        String payload = """
                {"eventId":"11111111-1111-1111-1111-111111111111","occurredAt":"2026-07-23T10:00:00Z",
                 "messageCreatedAt":"2026-07-23T10:00:00Z","eventType":"TRANSACTION_CREATED",
                 "payload":{"ticker":"SHOP","exchange":"NYSE","currency":"USD"}}
                """;
        StreamMessage<String, String, String> message = new StreamMessage<>(STREAM, "1-0", Map.of("payload", payload));

        when(recordStockUsageUseCase.execute(any()))
                .thenReturn(Uni.createFrom().item(new RecordStockUsageUseCase.Result.Success()));

        String ackedId = consumer.processMessage(message)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(ackedId).isEqualTo("1-0");

        ArgumentCaptor<RecordStockUsageUseCase.Command> commandCaptor =
                ArgumentCaptor.forClass(RecordStockUsageUseCase.Command.class);
        verify(recordStockUsageUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().ticker()).isEqualTo("SHOP");
        assertThat(commandCaptor.getValue().exchange()).isEqualTo("NYSE");
        assertThat(commandCaptor.getValue().currency()).isEqualTo("USD");

        verify(streamCommands).xack(STREAM, GROUP, "1-0");
    }

    @Test
    @DisplayName("Should acknowledge an unparseable message without calling the use case (avoids poison loop)")
    void shouldAcknowledgeUnparseableMessageWithoutProcessing() {
        StreamMessage<String, String, String> message =
                new StreamMessage<>(STREAM, "2-0", Map.of("payload", "not valid json"));

        String ackedId = consumer.processMessage(message)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(ackedId).isEqualTo("2-0");
        verifyNoInteractions(recordStockUsageUseCase);
        verify(streamCommands).xack(STREAM, GROUP, "2-0");
    }

    @Test
    @DisplayName("Should acknowledge a message missing the payload field entirely")
    void shouldAcknowledgeMessageMissingPayloadField() {
        StreamMessage<String, String, String> message =
                new StreamMessage<>(STREAM, "3-0", Map.of("someOtherField", "x"));

        String ackedId = consumer.processMessage(message)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(ackedId).isEqualTo("3-0");
        verifyNoInteractions(recordStockUsageUseCase);
        verify(streamCommands).xack(STREAM, GROUP, "3-0");
    }

    @Test
    @DisplayName("Should acknowledge a message whose payload is missing a ticker")
    void shouldAcknowledgeMessageMissingTicker() {
        String payload = """
                {"eventId":"11111111-1111-1111-1111-111111111111","occurredAt":"2026-07-23T10:00:00Z",
                 "messageCreatedAt":"2026-07-23T10:00:00Z","eventType":"TRANSACTION_CREATED",
                 "payload":{"exchange":"NYSE","currency":"USD"}}
                """;
        StreamMessage<String, String, String> message = new StreamMessage<>(STREAM, "4-0", Map.of("payload", payload));

        String ackedId = consumer.processMessage(message)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(ackedId).isEqualTo("4-0");
        verifyNoInteractions(recordStockUsageUseCase);
        verify(streamCommands).xack(STREAM, GROUP, "4-0");
    }
}
