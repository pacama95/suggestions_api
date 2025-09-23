package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NameContainsStrategy Tests")
class NameContainsStrategyTest {

    private NameContainsStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new NameContainsStrategy();
        testStocks = List.of(
            createStock("AAPL", "Apple Inc."),
            createStock("BAAPL", "Beta Apple Corporation"),
            createStock("MSFT", "Microsoft Corporation"),
            createStock("SOFT", "Software Inc."),
            createStock("APP", "App Inc."),
            createStock("APPL", "Apple Alternative Corp"),
            createStock("SNAP", "Apple Snap Corp"),
            createStock("TEST", "testing apple solutions"), // Lowercase to test case sensitivity
            createStock("GOOG", "Google LLC"),
            createStock("META", "Meta Platforms Inc.")
        );
    }

    @Test
    @DisplayName("Should return stocks whose names contain query")
    void shouldReturnNamesContainingQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Apple");

        // Then
        assertThat(result).hasSize(5); // Apple Inc., Beta Apple Corporation, Apple Alternative Corp, Apple Snap Corp, testing apple solutions
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().contains("APPLE"));
        assertThat(result).extracting(Stock::name)
                .containsExactlyInAnyOrder(
                    "Apple Inc.", 
                    "Beta Apple Corporation", 
                    "Apple Alternative Corp", 
                    "Apple Snap Corp", 
                    "testing apple solutions"
                );
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        // When
        List<Stock> lowerCaseResult = strategy.matches(testStocks, "apple");
        List<Stock> upperCaseResult = strategy.matches(testStocks, "APPLE");
        List<Stock> mixedCaseResult = strategy.matches(testStocks, "ApPlE");

        // Then
        assertThat(lowerCaseResult).hasSize(5);
        assertThat(lowerCaseResult).allMatch(stock -> stock.name().toUpperCase().contains("APPLE"));
        
        assertThat(upperCaseResult).hasSize(5);
        assertThat(upperCaseResult).allMatch(stock -> stock.name().toUpperCase().contains("APPLE"));
        
        assertThat(mixedCaseResult).hasSize(5);
        assertThat(mixedCaseResult).allMatch(stock -> stock.name().toUpperCase().contains("APPLE"));
    }

    @Test
    @DisplayName("Should handle exact matches as contains")
    void shouldHandleExactMatchesAsContains() {
        // When - Exact match should also be considered as contains
        List<Stock> result = strategy.matches(testStocks, "Apple Inc.");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Apple Inc.");
    }

    @Test
    @DisplayName("Should return empty list when no names contain query")
    void shouldReturnEmptyListWhenNoMatch() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Zebra");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle partial matches in middle of name")
    void shouldHandlePartialMatchesInMiddle() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Corp");

        // Then
        assertThat(result).hasSize(4); // Beta Apple Corporation, Microsoft Corporation, Apple Alternative Corp, Apple Snap Corp
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().contains("CORP"));
    }

    @Test
    @DisplayName("Should handle single character searches")
    void shouldHandleSingleCharacterSearches() {
        // When
        List<Stock> result = strategy.matches(testStocks, "a");

        // Then
        assertThat(result).hasSize(9); // Most names contain 'a' or 'A' (all except Microsoft and Google)
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().contains("A"));
    }

    @Test
    @DisplayName("Should handle multi-word queries")
    void shouldHandleMultiWordQueries() {
        // When
        List<Stock> result = strategy.matches(testStocks, "Apple Corp");

        // Then
        assertThat(result).hasSize(1); // Only "Beta Apple Corporation" contains "Apple Corp"
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().contains("APPLE CORP"));
    }

    @Test
    @DisplayName("Should trim whitespace from query")
    void shouldTrimWhitespaceFromQuery() {
        // When
        List<Stock> result = strategy.matches(testStocks, "  Apple  ");

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(stock -> stock.name().toUpperCase().contains("APPLE"));
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
    @DisplayName("Should handle special characters and punctuation")
    void shouldHandleSpecialCharactersAndPunctuation() {
        // Given
        List<Stock> specialStocks = List.of(
            createStock("DOT", "Apple.com Inc."),
            createStock("DASH", "Apple-Tech Corp"),
            createStock("SPACE", "Apple Inc Corp")
        );

        // When
        List<Stock> result = strategy.matches(specialStocks, "Apple.com");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Apple.com Inc.");
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // Then
        assertThat(strategy.priority()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should return correct description")
    void shouldReturnCorrectDescription() {
        // Then
        assertThat(strategy.description()).isEqualTo("Name contains query");
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
