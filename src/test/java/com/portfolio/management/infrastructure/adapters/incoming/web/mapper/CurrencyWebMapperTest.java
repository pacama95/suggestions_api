package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.CurrencyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyWebMapperTest {

    private CurrencyWebMapper currencyWebMapper;

    @BeforeEach
    void setUp() {
        currencyWebMapper = Mappers.getMapper(CurrencyWebMapper.class);
    }

    @Test
    void shouldMapCurrencyToResponseDto() {
        // Given
        Currency currency = Currency.of(1L, "USD", "United States Dollar", "$", "US", true);

        // When
        CurrencyResponse.CurrencyDto result = currencyWebMapper.toCurrencyDto(currency);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("USD");
        assertThat(result.name()).isEqualTo("United States Dollar");
        assertThat(result.symbol()).isEqualTo("$");
        assertThat(result.countryCode()).isEqualTo("US");
        assertThat(result.active()).isTrue(); // mapped from isActive
    }

    @Test
    void shouldMapCurrencyListToResponseDtoList() {
        // Given
        List<Currency> currencies = List.of(
                Currency.of(1L, "USD", "United States Dollar", "$", "US", true),
                Currency.of(2L, "EUR", "Euro", "€", "EU", true),
                Currency.of(3L, "JPY", "Japanese Yen", "¥", "JP", false)
        );

        // When
        List<CurrencyResponse.CurrencyDto> result = currencyWebMapper.toCurrencyDtoList(currencies);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).code()).isEqualTo("USD");
        assertThat(result.get(0).name()).isEqualTo("United States Dollar");
        assertThat(result.get(0).symbol()).isEqualTo("$");
        assertThat(result.get(0).countryCode()).isEqualTo("US");
        assertThat(result.get(0).active()).isTrue();
        
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).code()).isEqualTo("EUR");
        assertThat(result.get(1).name()).isEqualTo("Euro");
        assertThat(result.get(1).symbol()).isEqualTo("€");
        assertThat(result.get(1).countryCode()).isEqualTo("EU");
        assertThat(result.get(1).active()).isTrue();
        
        assertThat(result.get(2).id()).isEqualTo(3L);
        assertThat(result.get(2).code()).isEqualTo("JPY");
        assertThat(result.get(2).name()).isEqualTo("Japanese Yen");
        assertThat(result.get(2).symbol()).isEqualTo("¥");
        assertThat(result.get(2).countryCode()).isEqualTo("JP");
        assertThat(result.get(2).active()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenMappingEmptyCurrencyList() {
        // Given
        List<Currency> emptyCurrencyList = List.of();

        // When
        List<CurrencyResponse.CurrencyDto> result = currencyWebMapper.toCurrencyDtoList(emptyCurrencyList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapCurrencyListToCurrencyResponse() {
        // Given
        List<Currency> currencies = List.of(
                Currency.of(1L, "GBP", "British Pound Sterling", "£", "GB", true),
                Currency.of(2L, "CHF", "Swiss Franc", "CHF", "CH", true)
        );

        // When
        CurrencyResponse result = currencyWebMapper.toCurrencyResponse(currencies);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.currencies()).hasSize(2);
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.currencies().get(0).code()).isEqualTo("GBP");
        assertThat(result.currencies().get(1).code()).isEqualTo("CHF");
    }

    @Test
    void shouldMapSingleCurrencyToCurrencyResponse() {
        // Given
        Currency currency = Currency.of(1L, "CAD", "Canadian Dollar", "C$", "CA", true);

        // When
        CurrencyResponse result = currencyWebMapper.toCurrencyResponse(currency);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.currencies()).hasSize(1);
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.currencies().get(0).id()).isEqualTo(1L);
        assertThat(result.currencies().get(0).code()).isEqualTo("CAD");
        assertThat(result.currencies().get(0).name()).isEqualTo("Canadian Dollar");
        assertThat(result.currencies().get(0).symbol()).isEqualTo("C$");
        assertThat(result.currencies().get(0).countryCode()).isEqualTo("CA");
        assertThat(result.currencies().get(0).active()).isTrue();
    }

    @Test
    void shouldHandleMappingCurrencyWithNullSymbol() {
        // Given
        Currency currencyWithNullSymbol = new Currency(1L, "BTC", "Bitcoin", null, "XX", true);

        // When
        CurrencyResponse.CurrencyDto result = currencyWebMapper.toCurrencyDto(currencyWithNullSymbol);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("BTC");
        assertThat(result.name()).isEqualTo("Bitcoin");
        assertThat(result.symbol()).isNull();
        assertThat(result.countryCode()).isEqualTo("XX");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldMapInactiveCurrencyCorrectly() {
        // Given
        Currency inactiveCurrency = Currency.of(1L, "OLD", "Old Currency", "O", "ZZ", false);

        // When
        CurrencyResponse.CurrencyDto result = currencyWebMapper.toCurrencyDto(inactiveCurrency);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("OLD");
        assertThat(result.name()).isEqualTo("Old Currency");
        assertThat(result.symbol()).isEqualTo("O");
        assertThat(result.countryCode()).isEqualTo("ZZ");
        assertThat(result.active()).isFalse(); // mapped from isActive = false
    }

    @Test
    void shouldHandleMajorCurrencies() {
        // Given
        List<Currency> majorCurrencies = List.of(
                Currency.of(1L, "USD", "United States Dollar", "$", "US", true),
                Currency.of(2L, "EUR", "Euro", "€", "EU", true),
                Currency.of(3L, "JPY", "Japanese Yen", "¥", "JP", true),
                Currency.of(4L, "GBP", "British Pound Sterling", "£", "GB", true)
        );

        // When
        CurrencyResponse result = currencyWebMapper.toCurrencyResponse(majorCurrencies);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.currencies()).hasSize(4);
        assertThat(result.count()).isEqualTo(4);
        
        // Verify all are major currencies
        result.currencies().forEach(dto -> {
            assertThat(dto.code()).isIn("USD", "EUR", "JPY", "GBP");
            assertThat(dto.active()).isTrue();
        });
    }
}
