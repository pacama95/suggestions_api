package com.portfolio.management.infrastructure.adapters.outgoing.client;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
import com.portfolio.management.infrastructure.adapters.outgoing.client.mapper.StockMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataAdapterTest {

    @Mock
    private TwelveDataClient mockClient;

    @Mock
    private StockMapper mockStockMapper;

    private MarketDataAdapter marketDataAdapter;
    private final String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        marketDataAdapter = new MarketDataAdapter(mockClient, mockStockMapper, apiKey);
    }

    @Test
    void shouldReturnStocksWhenApiCallSucceeds() {
        // Given
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("MSFT", "Microsoft Corporation")
        );

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        // When
        List<Stock> result = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
        assertThat(result.get(0).name()).isEqualTo("Apple Inc.");
        assertThat(result.get(1).symbol()).isEqualTo("MSFT");
        assertThat(result.get(1).name()).isEqualTo("Microsoft Corporation");

        verify(mockClient).getAllStocks(eq(apiKey));
        verify(mockStockMapper).toStocks(eq(apiResponse));
    }

    @Test
    void shouldMapApiResponseToStocksCorrectly() {
        // Given
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(
                createStock("GOOGL", "Alphabet Inc."),
                createStock("TSLA", "Tesla Inc.")
        );

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        // When
        List<Stock> result = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify mapping was called with correct response
        verify(mockStockMapper).toStocks(eq(apiResponse));

        // Verify mapped stocks have expected properties
        assertThat(result.get(0).symbol()).isEqualTo("GOOGL");
        assertThat(result.get(0).currency()).isEqualTo("USD");
        assertThat(result.get(0).exchange()).isEqualTo("NASDAQ");
        assertThat(result.get(0).country()).isEqualTo("United States");

        assertThat(result.get(1).symbol()).isEqualTo("TSLA");
        assertThat(result.get(1).currency()).isEqualTo("USD");
        assertThat(result.get(1).exchange()).isEqualTo("NASDAQ");
        assertThat(result.get(1).country()).isEqualTo("United States");
    }

    @Test
    void shouldHandleEmptyApiResponse() {
        // Given
        var emptyApiResponse = new TwelveDataStockResponse(List.of(), "ok");
        var emptyStockList = Collections.emptyList();

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(emptyApiResponse));

        // When
        List<Stock> result = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(mockClient).getAllStocks(eq(apiKey));
        verify(mockStockMapper).toStocks(eq(emptyApiResponse));
    }

    @Test
    void shouldHandleErrorWhenApiClientFails() {
        // Given
        when(mockClient.getAllStocks(apiKey))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("API call failed")));

        // When
        Throwable exception = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("API call failed");

        verify(mockClient).getAllStocks(eq(apiKey));
    }

    @Test
    void shouldHandleErrorWhenMappingFails() {
        // Given
        var apiResponse = createMockApiResponse();

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When
        Throwable exception = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Mapping failed");

        verify(mockClient).getAllStocks(eq(apiKey));
        verify(mockStockMapper).toStocks(eq(apiResponse));
    }

    @Test
    void shouldUseCorrectApiKey() {
        // Given
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(createStock("NFLX", "Netflix Inc."));

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        // When
        marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        verify(mockClient).getAllStocks(eq(apiKey));
    }

    @Test
    void shouldHandleLargeStockListFromApi() {
        // Given
        var largeApiResponse = createLargeMockApiResponse();
        var largeStockList = createLargeStockList();

        when(mockClient.getAllStocks(apiKey)).thenReturn(Uni.createFrom().item(largeApiResponse));
        when(mockStockMapper.toStocks(largeApiResponse)).thenReturn(largeStockList);

        // When
        List<Stock> result = marketDataAdapter.fetchAllStocks()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1000); // Large list

        verify(mockClient).getAllStocks(eq(apiKey));
        verify(mockStockMapper).toStocks(eq(largeApiResponse));
    }

    private TwelveDataStockResponse createMockApiResponse() {
        var stock1 = new TwelveDataStockResponse.TwelveDataStock(
                "AAPL", "Apple Inc.", "USD", "NASDAQ", "XNAS",
                "United States", "Common Stock", "BBG000B9XRY4",
                "ESXXXX", "US0378331005", "037833100"
        );
        var stock2 = new TwelveDataStockResponse.TwelveDataStock(
                "MSFT", "Microsoft Corporation", "USD", "NASDAQ", "XNAS",
                "United States", "Common Stock", "BBG000BPH459",
                "ESXXXX", "US5949181045", "594918104"
        );

        return new TwelveDataStockResponse(List.of(stock1, stock2), "ok");
    }

    private TwelveDataStockResponse createLargeMockApiResponse() {
        return new TwelveDataStockResponse(List.of(), "ok");
    }

    private List<Stock> createLargeStockList() {
        return java.util.Collections.nCopies(1000, createStock("TEST", "Test Stock"));
    }

    private Stock createStock(String symbol, String name) {
        return new Stock(
                symbol,
                name,
                "USD",
                "NASDAQ",
                "XNAS",
                "United States",
                "Common Stock",
                "BBG123456789",
                "ESXXXX",
                "US1234567890",
                "123456789",
                1L
        );
    }
}
