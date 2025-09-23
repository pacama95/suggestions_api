package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SymbolStartsWithStrategy Tests")
class SymbolStartsWithStrategyTest {

    private SymbolStartsWithStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new SymbolStartsWithStrategy();
        testStocks = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("AMZN", "Amazon.com Inc."),
            createStock("MSFT", "Microsoft Corporation"),
            createStock("APP", "App Inc."),
            createStock("APPL", "Apple Alternative Corp"),
            createStock("aa", "Test Stock"), // Lowercase to test case sensitivity
            createStock("GOOG", "Google LLC"),
            createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return stocks whose symbols start with query")
    void shouldReturnSymbolsStartingWithQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "A");

        // Then
        assertThat(result).hasSize(5); // AAPL, AMZN, APP, APPL, aa
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().startsWith("A"));
        assertThat(result).extracting(Stock::symbol).containsExactlyInAnyOrder("AAPL", "AMZN", "APP", "APPL", "aa");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "aa");
        List<Stock> upperCaseResult = strategy.matches(testStocks, "AA");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "Aa");

        // Then - Should match AAPL and aa (case insensitive)
        assertThat(lowerCaseResult).hasSize(2);
        assertThat(lowerCaseResult).extracting(Stock::symbol).containsExactlyInAnyOrder("AAPL", "aa");
        
        assertThat(upperCaseResult).hasSize(2);
        assertThat(upperCaseResult).extracting(Stock::symbol).containsExactlyInAnyOrder("AAPL", "aa");
        
        assertThat(mixedCaseResult).hasSize(2);
        assertThat(mixedCaseResult).extracting(Stock::symbol).containsExactlyInAnyOrder("AAPL", "aa");
    }

    @Test
    @DisplayName("Should handle exact matches as starts-with")
    void shouldHandleExactMatchesAsStartsWith() {
        // When - Exact match should also be considered as starts-with
        List<Stock> result = strategy.matches(testStocks, "APP");

        // Then
        assertThat(result).hasSize(2); // APP and APPL
        assertThat(result).extracting(Stock::symbol).containsExactlyInAnyOrder("APP", "APPL");
    }

    @Test
    @DisplayName("Should return empty list when no symbols start with query")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Z");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle multi-character prefixes")
    void shouldHandleMultiCharacterPrefixes() {
        // When
        List<Stock> result = strategy.matches(testStocks, "APP");

        // Then
        assertThat(result).hasSize(2); // APP, APPL
        assertThat(result).extracting(Stock::symbol).containsExactlyInAnyOrder("APP", "APPL");
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  A  ");

        // Then
        assertThat(result).hasSize(5); // AAPL, AMZN, APP, APPL, aa
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().startsWith("A"));
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
        List<Stock> result = strategy.matches(List.of(), "A");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Symbol starts with query");
    }

    @Test
    @DisplayName("Should return correct search field")
    void shouldReturnCorrectSearchField() {
        // Then
        assertThat(strategy.searchField()).isEqualTo(SearchField.SYMBOL);
    }

    private Stock createStock(String symbol, String name) {
        return Stock.of(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", "ISIN", "CUSIP", 1L);
    }
}
