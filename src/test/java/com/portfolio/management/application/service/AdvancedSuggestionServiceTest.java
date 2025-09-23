package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.domain.strategy.priority.PriorityStrategy;
import com.portfolio.management.domain.strategy.priority.SearchField;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdvancedSuggestionService Tests")
class AdvancedSuggestionServiceTest {

    @Mock(lenient = true)
    StockPort mockStockPort;

    @Mock(lenient = true)
    Instance<PriorityStrategy> mockStrategyInstances;

    @Mock(lenient = true)
    PriorityStrategy mockSymbolStrategy;

    @Mock(lenient = true)
    PriorityStrategy mockNameStrategy;

    private AdvancedSuggestionService service;
    private List<Stock> testStocks;

    @BeforeEach
    void setUp() {
        // Mock strategy setup with lenient mode to avoid UnnecessaryStubbingException
        lenient().when(mockSymbolStrategy.priority()).thenReturn(1);
        lenient().when(mockSymbolStrategy.searchField()).thenReturn(SearchField.SYMBOL);
        lenient().when(mockSymbolStrategy.description()).thenReturn("Exact symbol match");

        lenient().when(mockNameStrategy.priority()).thenReturn(2);
        lenient().when(mockNameStrategy.searchField()).thenReturn(SearchField.NAME);
        lenient().when(mockNameStrategy.description()).thenReturn("Exact name match");

        lenient().when(mockStrategyInstances.stream()).thenReturn(Stream.of(mockSymbolStrategy, mockNameStrategy));

        service = new AdvancedSuggestionService(mockStockPort, mockStrategyInstances);

        // Test data
        testStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("MSFT", "Microsoft Corporation"),
                createStock("GOOG", "Google LLC")
        );
    }

    private GetSuggestionsAdvancedUseCase.Query createAdvancedQuery(String symbol, String companyName,
                                                                    String exchange, String country,
                                                                    String currency, int limit) {
        return new GetSuggestionsAdvancedUseCase.Query(symbol, companyName, exchange, country, currency, limit);
    }

    @Test
    @DisplayName("Should return success with results when valid query and matches found")
    void shouldReturnSuccessWithResultsWhenValidQuery() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("AAPL", null, null, null, null, 10);

        when(mockStockPort.findByAdvancedSearch(eq("AAPL"), isNull(), isNull(), isNull(), isNull(), eq(30)))
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst())); // Return AAPL match

        when(mockNameStrategy.matches(any(), any()))
                .thenReturn(List.of()); // No name matches

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.Success.class);

        GetSuggestionsAdvancedUseCase.Result.Success successResult =
                (GetSuggestionsAdvancedUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1);
        assertThat(successResult.suggestions().getFirst().symbol()).isEqualTo("AAPL");
        assertThat(successResult.count()).isEqualTo(1);
        assertThat(successResult.query()).contains("AAPL");
    }

    @Test
    @DisplayName("Should return success with empty results when no matches found")
    void shouldReturnSuccessWithEmptyResultsWhenNoMatches() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("NONEXISTENT", null, null, null, null, 10);

        when(mockStockPort.findByAdvancedSearch(eq("NONEXISTENT"), isNull(), isNull(), isNull(), isNull(), eq(30)))
                .thenReturn(Uni.createFrom().item(List.of()));

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.Success.class);

        GetSuggestionsAdvancedUseCase.Result.Success successResult =
                (GetSuggestionsAdvancedUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).isEmpty();
        assertThat(successResult.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return validation error when query has no search criteria")
    void shouldReturnValidationErrorWhenNoSearchCriteria() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery(null, null, null, null, null, 10);

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.ValidationError.class);

        GetSuggestionsAdvancedUseCase.Result.ValidationError validationError =
                (GetSuggestionsAdvancedUseCase.Result.ValidationError) result;

        assertThat(validationError.errors().errors()).hasSize(1);
        assertThat(validationError.errors().errors().getFirst().field()).isEqualTo("search_criteria");
        assertThat(validationError.errors().errors().getFirst().message()).isEqualTo("At least one search parameter must be provided");
        assertThat(validationError.errors().errors().getFirst().code()).isEqualTo("NO_SEARCH_CRITERIA");
    }

    @Test
    @DisplayName("Should return validation error when query has only empty strings")
    void shouldReturnValidationErrorWhenQueryHasOnlyEmptyStrings() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("", "", "", "", "", 10);

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.ValidationError.class);
    }

    @Test
    @DisplayName("Should return system error when repository fails")
    void shouldReturnSystemErrorWhenRepositoryFails() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("AAPL", null, null, null, null, 10);

        when(mockStockPort.findByAdvancedSearch(anyString(), any(), any(), any(), any(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database connection failed")));

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.SystemError.class);

        GetSuggestionsAdvancedUseCase.Result.SystemError systemError =
                (GetSuggestionsAdvancedUseCase.Result.SystemError) result;

        assertThat(systemError.errors().errors()).hasSize(1);
        assertThat(systemError.errors().errors().getFirst().field()).isEqualTo("repository");
        assertThat(systemError.errors().errors().getFirst().message()).isEqualTo("Failed to retrieve advanced suggestions");
        assertThat(systemError.errors().errors().getFirst().code()).isEqualTo("REPOSITORY_ERROR");
    }

    @Test
    @DisplayName("Should return success with multiple matches when query has multiple criteria")
    void shouldReturnSuccessWithMultipleMatches() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("AAPL", "Apple", null, null, null, 10);

        when(mockStockPort.findByAdvancedSearch(eq("AAPL"), eq("Apple"), isNull(), isNull(), isNull(), eq(30)))
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst())); // AAPL matches symbol

        when(mockNameStrategy.matches(eq(testStocks), eq("Apple")))
                .thenReturn(List.of(testStocks.getFirst())); // AAPL also matches name

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.Success.class);

        GetSuggestionsAdvancedUseCase.Result.Success successResult =
                (GetSuggestionsAdvancedUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1); // Same stock, no duplicates
        assertThat(successResult.suggestions().getFirst().symbol()).isEqualTo("AAPL");
        assertThat(successResult.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return success with limited results when candidates exceed limit")
    void shouldReturnSuccessWithLimitedResults() {
        // Given
        List<Stock> manyStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("APP", "App Inc."),
                createStock("APPL", "Apple Alternative")
        );

        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("APP", null, null, null, null, 2);

        when(mockStockPort.findByAdvancedSearch(eq("APP"), isNull(), isNull(), isNull(), isNull(), eq(6)))
                .thenReturn(Uni.createFrom().item(manyStocks));

        when(mockSymbolStrategy.matches(eq(manyStocks), eq("APP")))
                .thenReturn(manyStocks); // All match

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.Success.class);

        GetSuggestionsAdvancedUseCase.Result.Success successResult =
                (GetSuggestionsAdvancedUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(2); // Limited to requested limit
        assertThat(successResult.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should skip strategies when query field is empty")
    void shouldSkipStrategiesWhenQueryFieldIsEmpty() {
        // Given
        GetSuggestionsAdvancedUseCase.Query query = createAdvancedQuery("AAPL", null, null, null, null, 10);

        when(mockStockPort.findByAdvancedSearch(eq("AAPL"), isNull(), isNull(), isNull(), isNull(), eq(30)))
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst()));

        // Name strategy should be skipped since companyName is null

        // When
        GetSuggestionsAdvancedUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsAdvancedUseCase.Result.Success.class);

        GetSuggestionsAdvancedUseCase.Result.Success successResult =
                (GetSuggestionsAdvancedUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1);
        assertThat(successResult.suggestions().getFirst().symbol()).isEqualTo("AAPL");
    }

    private Stock createStock(String symbol, String name) {
        return Stock.of(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", "ISIN", "CUSIP", 1L);
    }
}