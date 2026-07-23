package com.portfolio.management.infrastructure.adapters.incoming.redis;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.redis")
public interface RedisStreamConfig {

    /**
     * If false, the consumer never starts; suggestions still serve on static
     * popularity alone. Useful for local/test environments without Redis.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Consumer group name. Kept distinct from other services' groups (e.g.
     * portfolio-api's) so each service independently receives every event.
     */
    @WithDefault("suggestions-consumers")
    String group();

    /**
     * Consumer name, should be unique per running instance.
     */
    @WithDefault("${HOSTNAME:local}-consumer")
    String consumerName();

    /**
     * Block time in milliseconds for XREADGROUP long-polling.
     */
    @WithDefault("60000")
    Long blockMs();

    /**
     * Number of messages to read per batch.
     */
    @WithDefault("50")
    Integer readCount();
}
