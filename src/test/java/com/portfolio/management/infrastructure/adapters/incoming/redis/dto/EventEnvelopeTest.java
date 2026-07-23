package com.portfolio.management.infrastructure.adapters.incoming.redis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies EventEnvelope/TransactionCreatedPayload deserialize the exact JSON
 * shape transactions-api publishes: Message&lt;TransactionCreatedData&gt;
 * serialized under a single "payload" stream field.
 */
class EventEnvelopeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    @DisplayName("Should deserialize a verbatim transactions-api TransactionCreated message")
    void shouldDeserializeTransactionCreatedMessage() throws Exception {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "occurredAt": "2026-07-23T10:00:00Z",
                  "messageCreatedAt": "2026-07-23T10:00:00Z",
                  "eventType": "TRANSACTION_CREATED",
                  "payload": {
                    "id": "22222222-2222-2222-2222-222222222222",
                    "ticker": "SHOP",
                    "transactionType": "BUY",
                    "assetType": "STOCK",
                    "quantity": 1,
                    "price": 100,
                    "fees": 0,
                    "currency": "USD",
                    "transactionDate": "2026-07-23",
                    "notes": null,
                    "isFractional": false,
                    "fractionalMultiplier": null,
                    "commissionCurrency": "USD",
                    "exchange": "NYSE",
                    "country": "US",
                    "companyName": "Shopify Inc"
                  }
                }
                """;

        EventEnvelope envelope = objectMapper.readValue(json, EventEnvelope.class);

        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.eventType()).isEqualTo("TRANSACTION_CREATED");
        assertThat(envelope.payload()).isNotNull();
        assertThat(envelope.payload().ticker()).isEqualTo("SHOP");
        assertThat(envelope.payload().exchange()).isEqualTo("NYSE");
        assertThat(envelope.payload().currency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should ignore unknown fields so the publisher can evolve its payload shape")
    void shouldIgnoreUnknownFields() throws Exception {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "occurredAt": "2026-07-23T10:00:00Z",
                  "messageCreatedAt": "2026-07-23T10:00:00Z",
                  "eventType": "TRANSACTION_CREATED",
                  "someBrandNewField": "should be ignored",
                  "payload": {
                    "ticker": "AAPL",
                    "exchange": "NASDAQ",
                    "currency": "USD",
                    "somethingUnexpected": 42
                  }
                }
                """;

        EventEnvelope envelope = objectMapper.readValue(json, EventEnvelope.class);

        assertThat(envelope.payload().ticker()).isEqualTo("AAPL");
    }
}
