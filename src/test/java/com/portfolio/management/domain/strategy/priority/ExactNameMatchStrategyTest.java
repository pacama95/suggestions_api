package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExactNameMatchStrategy Tests")
class ExactNameMatchStrategyTest {

    private ExactNameMatchStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new ExactNameMatchStrategy();
        testStocks = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("MSFT", "Microsoft Corporation"),
            createStock("APP", "App Inc."),
            createStock("APPL2", "apple computer corp"), // Different name to test case sensitivity
            createStock("GOOG", "Google LLC"),
            createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return exact match when name matches query exactly")
    void shouldReturnExactMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Apple Inc.");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("AAPL");
        assertThat(result.getFirst().name()).isEqualTo("Apple Inc.");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "apple inc.");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "ApPlE InC.");

        // Then
        assertThat(lowerCaseResult).hasSize(1);
        assertThat(lowerCaseResult.getFirst().name()).isEqualTo("Apple Inc.");
        
        assertThat(mixedCaseResult).hasSize(1);
        assertThat(mixedCaseResult.getFirst().name()).isEqualTo("Apple Inc.");
    }

    @Test
    @DisplayName("Should handle multiple exact matches")
    void shouldHandleMultipleExactMatches() {
        // Given - Add duplicate name
        List<Stock> stocksWithDuplicate = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("APPL", "Apple Inc."), // Different symbol, same name
            createStock("MSFT", "Microsoft Corporation")
        );

        // When
        List<Stock> result = strategy.matches(stocksWithDuplicate, "Apple Inc.");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(stock -> stock.name().equals("Apple Inc."));
    }

    @Test
    @DisplayName("Should return empty list when no exact match found")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Nonexistent Company");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not match partial names")
    void shouldNotMatchPartialNames() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Apple"); // Should not match "Apple Inc."

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  Apple Inc.  ");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Apple Inc.");
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
        List<Stock> result = strategy.matches(List.of(), "Apple Inc.");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Exact name match");
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
