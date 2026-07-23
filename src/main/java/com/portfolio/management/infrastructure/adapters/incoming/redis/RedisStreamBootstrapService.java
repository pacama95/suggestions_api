package com.portfolio.management.infrastructure.adapters.incoming.redis;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.stream.ReactiveStreamCommands;
import io.quarkus.redis.datasource.stream.XGroupCreateArgs;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.time.Duration;

/**
 * Bootstraps the transaction:created consumer group and starts the consumer.
 * Deliberately fails open: unlike a transactional-integrity service, suggestion
 * search must keep serving on the static popularity baseline even if Redis is
 * unreachable, so startup never blocks or throws on initialization failure.
 */
@ApplicationScoped
public class RedisStreamBootstrapService {

    private final ReactiveRedisDataSource redisDataSource;
    private final RedisStreamConfig config;
    private final TransactionCreatedConsumer transactionCreatedConsumer;

    private ReactiveStreamCommands<String, String, String> streamCommands;
    private Cancellable subscription;

    public RedisStreamBootstrapService(ReactiveRedisDataSource redisDataSource,
                                       RedisStreamConfig config,
                                       TransactionCreatedConsumer transactionCreatedConsumer) {
        this.redisDataSource = redisDataSource;
        this.config = config;
        this.transactionCreatedConsumer = transactionCreatedConsumer;
    }

    void onStart(@Observes StartupEvent event) {
        if (!config.enabled()) {
            Log.info("Redis stream consumer disabled (app.redis.enabled=false); " +
                    "suggestions will serve on the static popularity baseline only");
            return;
        }

        streamCommands = redisDataSource.stream(String.class, String.class, String.class);

        initializeConsumerGroup()
                .onFailure().retry().withBackOff(Duration.ofSeconds(2), Duration.ofSeconds(30)).atMost(5)
                .subscribe().with(
                        ignored -> {
                            Log.infof("Consumer group %s ready for stream %s",
                                    config.group(), TransactionCreatedConsumer.STREAM_NAME);
                            subscription = transactionCreatedConsumer.startConsuming();
                        },
                        failure -> Log.errorf(failure,
                                "Failed to initialize Redis stream consumer group after retries - " +
                                        "suggestions will keep serving on the static popularity baseline only " +
                                        "until the application is restarted"));
    }

    void onStop(@Observes ShutdownEvent event) {
        transactionCreatedConsumer.stop();
        if (subscription != null) {
            subscription.cancel();
        }
    }

    private Uni<Void> initializeConsumerGroup() {
        XGroupCreateArgs args = new XGroupCreateArgs().mkstream();

        return streamCommands.xgroupCreate(TransactionCreatedConsumer.STREAM_NAME, config.group(), "0", args)
                .onFailure().recoverWithUni(throwable -> {
                    // BUSYGROUP means the group already exists - fine, idempotent bootstrap.
                    if (throwable.getMessage() != null && throwable.getMessage().contains("BUSYGROUP")) {
                        return Uni.createFrom().voidItem();
                    }
                    return Uni.createFrom().failure(throwable);
                });
    }
}
