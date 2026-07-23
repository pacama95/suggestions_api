package com.portfolio.management.infrastructure.adapters.incoming.redis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Only the fields this consumer needs from transactions-api's
 * TransactionCreatedData payload. Unknown fields are ignored so the
 * publisher can evolve its event shape without breaking this consumer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionCreatedPayload(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("exchange") String exchange,
        @JsonProperty("currency") String currency
) {
}
