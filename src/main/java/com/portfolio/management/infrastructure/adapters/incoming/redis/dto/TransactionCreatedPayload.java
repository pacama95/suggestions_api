package com.portfolio.management.infrastructure.adapters.incoming.redis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Only the fields this consumer needs from the publisher's
 * TransactionCreatedData payload. Unknown fields are ignored so the
 * publisher can evolve its event shape without breaking this consumer.
 * <p>
 * Registered for reflection for the same reason as {@link EventEnvelope}: it
 * is only ever constructed by Jackson at runtime, which the native image build
 * cannot detect on its own.
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionCreatedPayload(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("exchange") String exchange,
        @JsonProperty("currency") String currency
) {
}
