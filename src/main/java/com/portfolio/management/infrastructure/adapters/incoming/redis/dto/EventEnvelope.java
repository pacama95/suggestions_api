package com.portfolio.management.infrastructure.adapters.incoming.redis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the generic message envelope the publisher wraps every Redis Streams
 * payload in: {eventId, occurredAt, messageCreatedAt, eventType, payload}.
 * <p>
 * Registered for reflection because this service ships as a native image and
 * nothing on a REST boundary references this record - it is only ever reached
 * through a runtime ObjectMapper.readValue, which GraalVM cannot see, so
 * without this the constructor is stripped and every message fails to
 * deserialize.
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("messageCreatedAt") Instant messageCreatedAt,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("payload") TransactionCreatedPayload payload
) {
}
