package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
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
@DisplayName("SuggestionService Tests")
class SuggestionServiceTest {

    @Mock(lenient = true)
    StockPort mockStockPort;

    @Mock(lenient = true)
    Instance<PriorityStrategy> mockStrategyInstances;

    @Mock(lenient = true)
    PriorityStrategy mockSymbolStrategy;

    @Mock(lenient = true)
    PriorityStrategy mockNameStrategy;

    private SuggestionService service;
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

        service = new SuggestionService(mockStockPort, mockStrategyInstances);

        // Test data
        testStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("MSFT", "Microsoft Corporation"),
                createStock("GOOG", "Google LLC")
        );
    }

    @Test
    @DisplayName("Should return success with results when valid query and matches found")
    void shouldReturnSuccessWithResultsWhenValidQuery() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("AAPL", 10);

        when(mockStockPort.findCandidateStocks(eq("AAPL"), eq(20))) // 10 * 2 (FETCH_MULTIPLIER)
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst())); // Return AAPL match

        when(mockNameStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of()); // No name matches

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);

        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1);
        assertThat(successResult.suggestions().getFirst().symbol()).isEqualTo("AAPL");
        assertThat(successResult.count()).isEqualTo(1);
        assertThat(successResult.query()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should return success with empty results when no matches found")
    void shouldReturnSuccessWithEmptyResultsWhenNoMatches() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("NONEXISTENT", 10);

        when(mockStockPort.findCandidateStocks(eq("NONEXISTENT"), eq(20)))
                .thenReturn(Uni.createFrom().item(List.of()));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);

        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).isEmpty();
        assertThat(successResult.count()).isEqualTo(0);
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

        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("APP", 2);

        when(mockStockPort.findCandidateStocks(eq("APP"), eq(4))) // 2 * 2
                .thenReturn(Uni.createFrom().item(manyStocks));

        when(mockSymbolStrategy.matches(eq(manyStocks), eq("APP")))
                .thenReturn(manyStocks); // All match

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);

        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(2); // Limited to requested limit
        assertThat(successResult.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should apply strategies in priority order")
    void shouldApplyStrategiesInPriorityOrder() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("Apple", 10);

        when(mockStockPort.findCandidateStocks(eq("Apple"), eq(20)))
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("Apple")))
                .thenReturn(List.of(testStocks.getFirst())); // AAPL matches symbol strategy

        when(mockNameStrategy.matches(eq(testStocks), eq("Apple")))
                .thenReturn(List.of(testStocks.getFirst())); // AAPL also matches name strategy

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);

        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1); // Same stock, no duplicates due to LinkedHashSet
        assertThat(successResult.suggestions().getFirst().symbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should return validation error when input is empty")
    void shouldReturnValidationErrorWhenInputIsEmpty() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("", 10);

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.ValidationError.class);

        GetSuggestionsUseCase.Result.ValidationError validationError =
                (GetSuggestionsUseCase.Result.ValidationError) result;

        assertThat(validationError.errors().errors()).hasSize(1);
        assertThat(validationError.errors().errors().getFirst().field()).isEqualTo("input");
        assertThat(validationError.errors().errors().getFirst().message()).isEqualTo("Search input cannot be empty");
        assertThat(validationError.errors().errors().getFirst().code()).isEqualTo("EMPTY_INPUT");
    }

    @Test
    @DisplayName("Should return validation error when input is whitespace only")
    void shouldReturnValidationErrorWhenInputIsWhitespaceOnly() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("   ", 10);

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.ValidationError.class);
    }

    @Test
    @DisplayName("Should return system error when repository fails")
    void shouldReturnSystemErrorWhenRepositoryFails() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("AAPL", 10);

        when(mockStockPort.findCandidateStocks(anyString(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database connection failed")));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.SystemError.class);

        GetSuggestionsUseCase.Result.SystemError systemError =
                (GetSuggestionsUseCase.Result.SystemError) result;

        assertThat(systemError.errors().errors()).hasSize(1);
        assertThat(systemError.errors().errors().getFirst().field()).isEqualTo("repository");
        assertThat(systemError.errors().errors().getFirst().message()).isEqualTo("Failed to retrieve suggestions");
        assertThat(systemError.errors().errors().getFirst().code()).isEqualTo("REPOSITORY_ERROR");
    }

    @Test
    @DisplayName("Should apply fetch multiplier correctly")
    void shouldApplyFetchMultiplierCorrectly() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("AAPL", 3);

        when(mockStockPort.findCandidateStocks(eq("AAPL"), eq(6))) // 3 * 2 (FETCH_MULTIPLIER)
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst()));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);
    }

    @Test
    @DisplayName("Should trim input query whitespace")
    void shouldTrimInputQueryWhitespace() {
        // Given
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("  AAPL  ", 10);

        when(mockStockPort.findCandidateStocks(eq("AAPL"), eq(20))) // Note: trimmed input
                .thenReturn(Uni.createFrom().item(testStocks));

        when(mockSymbolStrategy.matches(eq(testStocks), eq("AAPL")))
                .thenReturn(List.of(testStocks.getFirst()));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetSuggestionsUseCase.Result.Success.class);

        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(1);
        assertThat(successResult.query()).isEqualTo("AAPL"); // Query is trimmed in the service
    }

    @Test
    @DisplayName("Should rank the more popular stock first within the same priority tier")
    void shouldRankMorePopularStockFirstWithinSameTier() {
        // Given
        Stock lessPopular = createStockWithPopularity("APPH", "Apple Hospitality REIT", 10.0);
        Stock morePopular = createStockWithPopularity("AAPL", "Apple Inc.", 120.0);
        List<Stock> candidates = List.of(lessPopular, morePopular);

        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("Apple", 10);

        when(mockStockPort.findCandidateStocks(eq("Apple"), eq(20)))
                .thenReturn(Uni.createFrom().item(candidates));

        // Both match the same (name) strategy tier, in "database order" (less popular first)
        when(mockSymbolStrategy.matches(eq(candidates), eq("Apple")))
                .thenReturn(List.of());
        when(mockNameStrategy.matches(eq(candidates), eq("Apple")))
                .thenReturn(List.of(lessPopular, morePopular));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(2);
        assertThat(successResult.suggestions().get(0).symbol()).isEqualTo("AAPL");
        assertThat(successResult.suggestions().get(1).symbol()).isEqualTo("APPH");
    }

    @Test
    @DisplayName("Should never let a popular fuzzy match outrank an exact match from a higher-priority tier")
    void shouldNeverLetPopularityOverrideMatchQualityPriority() {
        // Given
        Stock exactMatch = createStockWithPopularity("APP", "App Inc.", 0.0); // unpopular exact symbol match
        Stock popularFuzzyMatch = createStockWithPopularity("AAPL", "Apple Inc.", 100.0); // popular but lower-priority tier

        List<Stock> candidates = List.of(exactMatch, popularFuzzyMatch);
        GetSuggestionsUseCase.Query query = new GetSuggestionsUseCase.Query("APP", 10);

        when(mockStockPort.findCandidateStocks(eq("APP"), eq(20)))
                .thenReturn(Uni.createFrom().item(candidates));

        // Symbol strategy (priority 1) matches only the unpopular exact match
        when(mockSymbolStrategy.matches(eq(candidates), eq("APP")))
                .thenReturn(List.of(exactMatch));
        // Name strategy (priority 2) matches only the popular fuzzy match
        when(mockNameStrategy.matches(eq(candidates), eq("APP")))
                .thenReturn(List.of(popularFuzzyMatch));

        // When
        GetSuggestionsUseCase.Result result = service.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        GetSuggestionsUseCase.Result.Success successResult =
                (GetSuggestionsUseCase.Result.Success) result;

        assertThat(successResult.suggestions()).hasSize(2);
        assertThat(successResult.suggestions().get(0).symbol()).isEqualTo("APP");
        assertThat(successResult.suggestions().get(1).symbol()).isEqualTo("AAPL");
    }

    private Stock createStock(String symbol, String name) {
        return Stock.of(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", "ISIN", "CUSIP", 1L);
    }

    private Stock createStockWithPopularity(String symbol, String name, double popularityScore) {
        return new Stock(1L, symbol, name, "USD", "NYSE", "MIC", "US", "CS", "FIGI", "CFI", "ISIN", "CUSIP", 1L, popularityScore);
    }
}