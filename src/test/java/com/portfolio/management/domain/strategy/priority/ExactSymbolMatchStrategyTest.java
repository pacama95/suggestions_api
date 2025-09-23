package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExactSymbolMatchStrategy Tests")
class ExactSymbolMatchStrategyTest {

    private ExactSymbolMatchStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new ExactSymbolMatchStrategy();
        testStocks = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("MSFT", "Microsoft Corporation"),
            createStock("APP", "App Inc."),
            createStock("apple", "Apple Computer Corp"), // Lowercase to test case sensitivity
            createStock("GOOG", "Google LLC"),
            createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return exact match when symbol matches query exactly")
    void shouldReturnExactMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "AAPL");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("AAPL");
        assertThat(result.getFirst().name()).isEqualTo("Apple Inc.");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "aapl");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "AaPl");

        // Then
        assertThat(lowerCaseResult).hasSize(1);
        assertThat(lowerCaseResult.getFirst().symbol()).isEqualTo("AAPL");
        
        assertThat(mixedCaseResult).hasSize(1);
        assertThat(mixedCaseResult.getFirst().symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should handle multiple exact matches")
    void shouldHandleMultipleExactMatches() {
        // Given - Add duplicate symbol with different case
        List<Stock> stocksWithDuplicate = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("AAPL", "Apple Alternative Corp"),
            createStock("MSFT", "Microsoft Corporation")
        );

        // When
        List<Stock> result = strategy.matches(stocksWithDuplicate, "AAPL");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(stock -> stock.symbol().equals("AAPL"));
    }

    @Test
    @DisplayName("Should return empty list when no exact match found")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not match partial symbols")
    void shouldNotMatchPartialSymbols() {
        // When
        List<Stock> result = strategy.matches(testStocks, "AA"); // Should not match "AAPL"

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  AAPL  ");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("AAPL");
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
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Exact symbol match");
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
