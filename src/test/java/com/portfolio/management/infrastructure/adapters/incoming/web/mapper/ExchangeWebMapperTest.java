package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ExchangeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeWebMapperTest {

    private ExchangeWebMapper exchangeWebMapper;

    @BeforeEach
    void setUp() {
        exchangeWebMapper = Mappers.getMapper(ExchangeWebMapper.class);
    }

    @Test
    void shouldMapExchangeToResponseDto() {
        // Given
        Exchange exchange = Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true);

        // When
        ExchangeResponse.ExchangeDto result = exchangeWebMapper.toExchangeDto(exchange);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("NYSE");
        assertThat(result.name()).isEqualTo("New York Stock Exchange");
        assertThat(result.country()).isEqualTo("United States");
        assertThat(result.timezone()).isEqualTo("America/New_York");
        assertThat(result.currencyCode()).isEqualTo("USD");
        assertThat(result.active()).isTrue(); // mapped from isActive
    }

    @Test
    void shouldMapExchangeListToResponseDtoList() {
        // Given
        List<Exchange> exchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true),
                Exchange.of(3L, "TSE", "Tokyo Stock Exchange", "Japan", "Asia/Tokyo", "JPY", false)
        );

        // When
        List<ExchangeResponse.ExchangeDto> result = exchangeWebMapper.toExchangeDtoList(exchanges);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).code()).isEqualTo("NYSE");
        assertThat(result.get(0).name()).isEqualTo("New York Stock Exchange");
        assertThat(result.get(0).country()).isEqualTo("United States");
        assertThat(result.get(0).timezone()).isEqualTo("America/New_York");
        assertThat(result.get(0).currencyCode()).isEqualTo("USD");
        assertThat(result.get(0).active()).isTrue();
        
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).code()).isEqualTo("LSE");
        assertThat(result.get(1).name()).isEqualTo("London Stock Exchange");
        assertThat(result.get(1).country()).isEqualTo("United Kingdom");
        assertThat(result.get(1).timezone()).isEqualTo("Europe/London");
        assertThat(result.get(1).currencyCode()).isEqualTo("GBP");
        assertThat(result.get(1).active()).isTrue();
        
        assertThat(result.get(2).id()).isEqualTo(3L);
        assertThat(result.get(2).code()).isEqualTo("TSE");
        assertThat(result.get(2).name()).isEqualTo("Tokyo Stock Exchange");
        assertThat(result.get(2).country()).isEqualTo("Japan");
        assertThat(result.get(2).timezone()).isEqualTo("Asia/Tokyo");
        assertThat(result.get(2).currencyCode()).isEqualTo("JPY");
        assertThat(result.get(2).active()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenMappingEmptyExchangeList() {
        // Given
        List<Exchange> emptyExchangeList = List.of();

        // When
        List<ExchangeResponse.ExchangeDto> result = exchangeWebMapper.toExchangeDtoList(emptyExchangeList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapExchangeListToExchangeResponse() {
        // Given
        List<Exchange> exchanges = List.of(
                Exchange.of(1L, "NASDAQ", "NASDAQ Global Market", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "XETR", "Deutsche Boerse XETRA", "Germany", "Europe/Berlin", "EUR", true)
        );

        // When
        ExchangeResponse result = exchangeWebMapper.toExchangeResponse(exchanges);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.exchanges()).hasSize(2);
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.exchanges().get(0).code()).isEqualTo("NASDAQ");
        assertThat(result.exchanges().get(1).code()).isEqualTo("XETR");
    }

    @Test
    void shouldMapSingleExchangeToExchangeResponse() {
        // Given
        Exchange exchange = Exchange.of(1L, "ASX", "Australian Securities Exchange", "Australia", "Australia/Sydney", "AUD", true);

        // When
        ExchangeResponse result = exchangeWebMapper.toExchangeResponse(exchange);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.exchanges()).hasSize(1);
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.exchanges().get(0).id()).isEqualTo(1L);
        assertThat(result.exchanges().get(0).code()).isEqualTo("ASX");
        assertThat(result.exchanges().get(0).name()).isEqualTo("Australian Securities Exchange");
        assertThat(result.exchanges().get(0).country()).isEqualTo("Australia");
        assertThat(result.exchanges().get(0).timezone()).isEqualTo("Australia/Sydney");
        assertThat(result.exchanges().get(0).currencyCode()).isEqualTo("AUD");
        assertThat(result.exchanges().get(0).active()).isTrue();
    }

    @Test
    void shouldHandleMappingExchangeWithNullTimezone() {
        // Given
        Exchange exchangeWithNullTimezone = new Exchange(1L, "TST", "Test Exchange", "Test Country", null, "TST", true);

        // When
        ExchangeResponse.ExchangeDto result = exchangeWebMapper.toExchangeDto(exchangeWithNullTimezone);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("TST");
        assertThat(result.name()).isEqualTo("Test Exchange");
        assertThat(result.country()).isEqualTo("Test Country");
        assertThat(result.timezone()).isNull();
        assertThat(result.currencyCode()).isEqualTo("TST");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldMapInactiveExchangeCorrectly() {
        // Given
        Exchange inactiveExchange = Exchange.of(1L, "CLOSED", "Closed Exchange", "Nowhere", "UTC", "XXX", false);

        // When
        ExchangeResponse.ExchangeDto result = exchangeWebMapper.toExchangeDto(inactiveExchange);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("CLOSED");
        assertThat(result.name()).isEqualTo("Closed Exchange");
        assertThat(result.country()).isEqualTo("Nowhere");
        assertThat(result.timezone()).isEqualTo("UTC");
        assertThat(result.currencyCode()).isEqualTo("XXX");
        assertThat(result.active()).isFalse(); // mapped from isActive = false
    }

    @Test
    void shouldHandleMajorExchanges() {
        // Given
        List<Exchange> majorExchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "NASDAQ", "NASDAQ Global Market", "United States", "America/New_York", "USD", true),
                Exchange.of(3L, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true),
                Exchange.of(4L, "TSE", "Tokyo Stock Exchange", "Japan", "Asia/Tokyo", "JPY", true)
        );

        // When
        ExchangeResponse result = exchangeWebMapper.toExchangeResponse(majorExchanges);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.exchanges()).hasSize(4);
        assertThat(result.count()).isEqualTo(4);
        
        // Verify all are major exchanges
        result.exchanges().forEach(dto -> {
            assertThat(dto.code()).isIn("NYSE", "NASDAQ", "LSE", "TSE");
            assertThat(dto.active()).isTrue();
            assertThat(dto.country()).isIn("United States", "United Kingdom", "Japan");
        });
    }
}
