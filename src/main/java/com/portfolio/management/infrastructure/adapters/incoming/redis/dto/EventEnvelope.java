package com.portfolio.management.infrastructure.adapters.incoming.redis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the generic message envelope transactions-api wraps every
 * Redis Streams payload in: {eventId, occurredAt, messageCreatedAt,
 * eventType, payload}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("messageCreatedAt") Instant messageCreatedAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("payload") TransactionCreatedPayload payload
) {
}
