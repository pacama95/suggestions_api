package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SymbolContainsStrategy Tests")
class SymbolContainsStrategyTest {

    private SymbolContainsStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new SymbolContainsStrategy();
        testStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("BAAPL", "Beta Apple Corp"),
                createStock("MSFT", "Microsoft Corporation"),
                createStock("SOFT", "Software Inc."),
                createStock("APP", "App Inc."),
                createStock("APPL", "Apple Alternative Corp"),
                createStock("SNAPPL", "Snap Apple Corp"),
                createStock("xaaplx", "Test Apple Corp"), // Lowercase to test case sensitivity
                createStock("GOOG", "Google LLC"),
                createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return stocks whose symbols contain query")
    void shouldReturnSymbolsContainingQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "AAPL");

        // Then
        assertThat(result).hasSize(3); // AAPL, BAAPL, xaaplx (SNAPPL contains APPL not AAPL)
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().contains("AAPL"));
        assertThat(result).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("AAPL", "BAAPL", "xaaplx");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "aapl");
        List<Stock> upperCaseResult = strategy.matches(testStocks, "AAPL");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "AaPl");

        // Then
        assertThat(lowerCaseResult).hasSize(3);
        assertThat(lowerCaseResult).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("AAPL", "BAAPL", "xaaplx");

        assertThat(upperCaseResult).hasSize(3);
        assertThat(upperCaseResult).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("AAPL", "BAAPL", "xaaplx");

        assertThat(mixedCaseResult).hasSize(3);
        assertThat(mixedCaseResult).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("AAPL", "BAAPL", "xaaplx");
    }

    @Test
    @DisplayName("Should handle exact matches as contains")
    void shouldHandleExactMatchesAsContains() {
        // When - Exact match should also be considered as contains
        List<Stock> result = strategy.matches(testStocks, "APP");

        // Then
        assertThat(result).hasSize(3); // APP, APPL, SNAPPL (all contain "APP")
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().contains("APP"));
    }

    @Test
    @DisplayName("Should return empty list when no symbols contain query")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "XYZ");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle partial matches in middle of symbol")
    void shouldHandlePartialMatchesInMiddle() {
        // When
        List<Stock> result = strategy.matches(testStocks, "APP");

        // Then
        assertThat(result).hasSize(3); // APP, APPL, SNAPPL (symbols containing "APP")
        assertThat(result).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("APP", "APPL", "SNAPPL");
    }

    @Test
    @DisplayName("Should handle single character searches")
    void shouldHandleSingleCharacterSearches() {
        // When
        List<Stock> result = strategy.matches(testStocks, "A");

        // Then
        assertThat(result).hasSize(7); // All symbols containing 'A' (AAPL, BAAPL, APP, APPL, SNAPPL, xaaplx, META)
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().contains("A"));
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  AAPL  ");

        // Then
        assertThat(result).hasSize(3); // AAPL, BAAPL, xaaplx
        assertThat(result).allMatch(stock -> stock.symbol().toUpperCase().contains("AAPL"));
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
        List<Stock> result = strategy.matches(List.of(), "AAPL");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle overlapping matches")
    void shouldHandleOverlappingMatches() {
        // Given - stocks with overlapping patterns
        List<Stock> overlappingStocks = List.of(
                createStock("ABCD", "Test 1"),
                createStock("BCDA", "Test 2"),
                createStock("CDAB", "Test 3"),
                createStock("DABC", "Test 4")
        );

        // When
        List<Stock> result = strategy.matches(overlappingStocks, "AB");

        // Then
        assertThat(result).hasSize(3); // ABCD, CDAB, DABC
        assertThat(result).extracting(Stock::symbol)
                .containsExactlyInAnyOrder("ABCD", "CDAB", "DABC");
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Symbol contains query");
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
