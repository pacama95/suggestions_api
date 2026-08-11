package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExactIsinMatchStrategy Tests")
class ExactIsinMatchStrategyTest {

    private ExactIsinMatchStrategy strategy;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        strategy = new ExactIsinMatchStrategy();
        testStocks = List.of(
                createStock("AAPL", "Apple Inc.", "US0378331005"),
                createStock("MSFT", "Microsoft Corporation", "US5949181045"),
                createStock("GOOG", "Google LLC", null)
        );
    }

    @Test
    @DisplayName("Should return exact match when isin matches query exactly")
    void shouldReturnExactMatch() {
        List<Stock> result = strategy.matches(testStocks, "US0378331005");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
        List<Stock> result = strategy.matches(testStocks, "us0378331005");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should skip stocks with a null isin")
    void shouldSkipStocksWithNullIsin() {
        List<Stock> result = strategy.matches(testStocks, "SOMETHING");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for empty query")
    void shouldReturnEmptyListForEmptyQuery() {
        List<Stock> result = strategy.matches(testStocks, "   ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        assertThat(strategy.priority()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return correct search field")
    void shouldReturnCorrectSearchField() {
        assertThat(strategy.searchField()).isEqualTo(SearchField.ISIN);
    }

    private Stock createStock(String symbol, String name, String isin) {
        return new Stock(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", isin, "CUSIP", 1L, 0.0);
    }
}
