package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NameStartsWithStrategy Tests")
class NameStartsWithStrategyTest {

    private NameStartsWithStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new NameStartsWithStrategy();
        testStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("AMZN", "Amazon.com Inc."),
                createStock("MSFT", "Microsoft Corporation"),
                createStock("APP", "App Inc."),
                createStock("APPL", "Apple Alternative Corp"),
                createStock("GOV", "apple government corp"), // Lowercase to test case sensitivity
                createStock("GOOG", "Google LLC"),
                createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return stocks whose names start with query")
    void shouldReturnNamesStartingWithQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "A");

        // Then  
        assertThat(result).hasSize(5); // Apple Inc., Amazon.com Inc., App Inc., Apple Alternative Corp, apple government corp
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().startsWith("A"));
        assertThat(result).extracting(Stock::name)
                .containsExactlyInAnyOrder("Apple Inc.", "Amazon.com Inc.", "App Inc.", "Apple Alternative Corp", "apple government corp");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "apple");
        List<Stock> upperCaseResult = strategy.matches(testStocks, "APPLE");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "Apple");

        // Then - Should match "Apple Inc.", "Apple Alternative Corp", "apple government corp"
        assertThat(lowerCaseResult).hasSize(3);
        assertThat(lowerCaseResult).extracting(Stock::name)
                .containsExactlyInAnyOrder("Apple Inc.", "Apple Alternative Corp", "apple government corp");

        assertThat(upperCaseResult).hasSize(3);
        assertThat(upperCaseResult).extracting(Stock::name)
                .containsExactlyInAnyOrder("Apple Inc.", "Apple Alternative Corp", "apple government corp");

        assertThat(mixedCaseResult).hasSize(3);
        assertThat(mixedCaseResult).extracting(Stock::name)
                .containsExactlyInAnyOrder("Apple Inc.", "Apple Alternative Corp", "apple government corp");
    }

    @Test
    @DisplayName("Should handle exact matches as starts-with")
    void shouldHandleExactMatchesAsStartsWith() {
        // When - Exact match should also be considered as starts-with
        List<Stock> result = strategy.matches(testStocks, "Apple Inc.");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Apple Inc.");
    }

    @Test
    @DisplayName("Should return empty list when no names start with query")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Zebra");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle multi-word prefixes")
    void shouldHandleMultiWordPrefixes() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Apple A");

        // Then
        assertThat(result).hasSize(1); // Only "Apple Alternative Corp"
        assertThat(result.getFirst().name()).isEqualTo("Apple Alternative Corp");
    }

    @Test
    @DisplayName("Should handle single character searches")
    void shouldHandleSingleCharacterSearches() {
        // When
        List<Stock> result = strategy.matches(testStocks, "M");

        // Then
        assertThat(result).hasSize(2); // Microsoft Corporation, Meta Platforms Inc.
        assertThat(result).extracting(Stock::name)
                .containsExactlyInAnyOrder("Microsoft Corporation", "Meta Platforms Inc.");
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  Apple  ");

        // Then
        assertThat(result).hasSize(3); // All Apple-related companies
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().startsWith("APPLE"));
    }

    @Test
    @DisplayName("Should return empty list for empty query")
    void shouldReturnEmptyListForEmptyQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for whitespace-only query")
    void shouldReturnEmptyListForWhitespaceOnlyQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "   ");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty stock list")
    void shouldHandleEmptyStockList() {
        // When
        List<Stock> result = strategy.matches(List.of(), "Apple");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Name starts with query");
    }

    @Test
    @DisplayName("Should return correct search field")
    void shouldReturnCorrectSearchField() {
        // Then
        assertThat(strategy.searchField()).isEqualTo(SearchField.NAME);
    }

    private Stock createStock(String symbol, String name) {
        return Stock.of(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", "ISIN", "CUSIP", 1L);
    }
}
