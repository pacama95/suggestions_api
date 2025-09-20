package com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.CurrencyEntity;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.ExchangeEntity;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private CategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CategoryMapper.class);
    }

    @Test
    @DisplayName("Should map StockTypeEntity to StockType domain object")
    void testToStockType_Success() {
        // Given
        StockTypeEntity entity = new StockTypeEntity("ETF", "Exchange-Traded Fund", "Investment fund traded on stock exchanges");
        entity.id = 1L;

        // When
        StockType result = mapper.toStockType(entity);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.id());
        assertEquals("ETF", result.code());
        assertEquals("Exchange-Traded Fund", result.name());
        assertEquals("Investment fund traded on stock exchanges", result.description());
        assertTrue(result.isActive());
        assertTrue(result.isETF());
        assertFalse(result.isCommonStock());
    }

    @Test
    @DisplayName("Should map StockType domain object to StockTypeEntity")
    void testToStockTypeEntity_Success() {
        // Given
        StockType domainObject = StockType.of(2L, "REIT", "REIT", "Real Estate Investment Trust", true);

        // When
        StockTypeEntity result = mapper.toStockTypeEntity(domainObject);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.id);
        assertEquals("REIT", result.getCode());
        assertEquals("REIT", result.getName());
        assertEquals("Real Estate Investment Trust", result.getDescription());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("Should map CurrencyEntity to Currency domain object")
    void testToCurrency_Success() {
        // Given
        CurrencyEntity entity = new CurrencyEntity("USD", "US Dollar", "$", "US");
        entity.id = 3L;

        // When
        Currency result = mapper.toCurrency(entity);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(3L), result.id());
        assertEquals("USD", result.code());
        assertEquals("US Dollar", result.name());
        assertEquals("$", result.symbol());
        assertEquals("US", result.countryCode());
        assertTrue(result.isActive());
        assertTrue(result.isUSD());
        assertTrue(result.isMajorCurrency());
        assertEquals("$ USD", result.getDisplayName());
    }

    @Test
    @DisplayName("Should map Currency domain object to CurrencyEntity")
    void testToCurrencyEntity_Success() {
        // Given
        Currency domainObject = Currency.of(4L, "EUR", "Euro", "€", "EU", true);

        // When
        CurrencyEntity result = mapper.toCurrencyEntity(domainObject);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(4L), result.id);
        assertEquals("EUR", result.getCode());
        assertEquals("Euro", result.getName());
        assertEquals("€", result.getSymbol());
        assertEquals("EU", result.getCountryCode());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("Should map ExchangeEntity to Exchange domain object")
    void testToExchange_Success() {
        // Given
        ExchangeEntity entity = new ExchangeEntity("NYSE", "New York Stock Exchange", "United States", "EST", "USD");
        entity.id = 5L;

        // When
        Exchange result = mapper.toExchange(entity);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(5L), result.id());
        assertEquals("NYSE", result.code());
        assertEquals("New York Stock Exchange", result.name());
        assertEquals("United States", result.country());
        assertEquals("EST", result.timezone());
        assertEquals("USD", result.currencyCode());
        assertTrue(result.isActive());
        assertTrue(result.isUSExchange());
        assertFalse(result.isEuropeanExchange());
        assertFalse(result.isAsianExchange());
        assertEquals("North America", result.getRegion());
        assertEquals("New York Stock Exchange (United States)", result.getDisplayName());
    }

    @Test
    @DisplayName("Should map Exchange domain object to ExchangeEntity")
    void testToExchangeEntity_Success() {
        // Given
        Exchange domainObject = Exchange.of(6L, "LSE", "London Stock Exchange", "United Kingdom", "GMT", "GBP", true);

        // When
        ExchangeEntity result = mapper.toExchangeEntity(domainObject);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(6L), result.id);
        assertEquals("LSE", result.getCode());
        assertEquals("London Stock Exchange", result.getName());
        assertEquals("United Kingdom", result.getCountry());
        assertEquals("GMT", result.getTimezone());
        assertEquals("GBP", result.getCurrencyCode());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("Should handle null values properly")
    void testToStockType_WithNullEntity() {
        // When
        StockType result = mapper.toStockType(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle inactive entities")
    void testToCurrency_InactiveEntity() {
        // Given
        CurrencyEntity entity = new CurrencyEntity("TEST", "Test Currency", "T", "TE");
        entity.id = 7L;
        entity.setIsActive(false);

        // When
        Currency result = mapper.toCurrency(entity);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(7L), result.id());
        assertEquals("TEST", result.code());
        assertEquals("Test Currency", result.name());
        assertFalse(result.isActive());
        assertFalse(result.isMajorCurrency());
    }

    @Test
    @DisplayName("Should map exchange without timezone")
    void testToExchange_WithoutTimezone() {
        // Given
        ExchangeEntity entity = new ExchangeEntity("TSE", "Tokyo Stock Exchange", "Japan", null, "JPY");
        entity.id = 8L;

        // When
        Exchange result = mapper.toExchange(entity);

        // Then
        assertNotNull(result);
        assertEquals("TSE", result.code());
        assertEquals("Tokyo Stock Exchange", result.name());
        assertEquals("Japan", result.country());
        assertNull(result.timezone());
        assertEquals("JPY", result.currencyCode());
        assertTrue(result.isAsianExchange());
        assertEquals("Asia", result.getRegion());
    }
}
