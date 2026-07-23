package com.portfolio.management.infrastructure.adapters.outgoing.client;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
import com.portfolio.management.infrastructure.adapters.outgoing.client.mapper.StockMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
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
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("MSFT", "Microsoft Corporation")
        );

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        List<Stock> result = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
        assertThat(result.get(1).symbol()).isEqualTo("MSFT");

        verify(mockClient).getStocks(eq(apiKey), isNull(), isNull());
        verify(mockStockMapper).toStocks(eq(apiResponse));
    }

    @Test
    void shouldMapApiResponseToStocksCorrectly() {
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(
                createStock("GOOGL", "Alphabet Inc."),
                createStock("TSLA", "Tesla Inc.")
        );

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        List<Stock> result = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).symbol()).isEqualTo("GOOGL");
        assertThat(result.get(0).currency()).isEqualTo("USD");
        assertThat(result.get(0).exchange()).isEqualTo("NASDAQ");
        assertThat(result.get(0).country()).isEqualTo("United States");
    }

    @Test
    void shouldHandleEmptyApiResponse() {
        var emptyApiResponse = new TwelveDataStockResponse(List.of(), "ok");

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(emptyApiResponse));
        when(mockStockMapper.toStocks(emptyApiResponse)).thenReturn(Collections.emptyList());

        List<Stock> result = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).isEmpty();
        verify(mockClient).getStocks(eq(apiKey), isNull(), isNull());
        verify(mockStockMapper).toStocks(eq(emptyApiResponse));
    }

    @Test
    void shouldHandleErrorWhenApiClientFails() {
        when(mockClient.getStocks(eq(apiKey), isNull(), isNull()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("API call failed")));

        Throwable exception = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("API call failed");
    }

    @Test
    void shouldHandleErrorWhenMappingFails() {
        var apiResponse = createMockApiResponse();

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse))
                .thenThrow(new RuntimeException("Mapping failed"));

        Throwable exception = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Mapping failed");
    }

    @Test
    void shouldUseCorrectApiKey() {
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(createStock("NFLX", "Netflix Inc."));

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        verify(mockClient).getStocks(eq(apiKey), isNull(), isNull());
    }

    @Test
    void shouldHandleLargeStockListFromApi() {
        var largeApiResponse = createLargeMockApiResponse();
        var largeStockList = createLargeStockList();

        when(mockClient.getStocks(eq(apiKey), isNull(), isNull())).thenReturn(Uni.createFrom().item(largeApiResponse));
        when(mockStockMapper.toStocks(largeApiResponse)).thenReturn(largeStockList);

        List<Stock> result = marketDataAdapter.fetchStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(1000);
        verify(mockClient).getStocks(eq(apiKey), isNull(), isNull());
    }

    @Test
    @DisplayName("Should make one API call per exchange and country combination")
    void shouldMakeOneApiCallPerExchangeAndCountryCombination() {
        var nasdaqResponse = new TwelveDataStockResponse(List.of(), "nasdaq");
        var nyseResponse = new TwelveDataStockResponse(List.of(), "nyse");

        when(mockClient.getStocks(eq(apiKey), eq("NASDAQ"), eq("United States")))
                .thenReturn(Uni.createFrom().item(nasdaqResponse));
        when(mockClient.getStocks(eq(apiKey), eq("NYSE"), eq("United States")))
                .thenReturn(Uni.createFrom().item(nyseResponse));
        when(mockStockMapper.toStocks(nasdaqResponse))
                .thenReturn(List.of(createStock("AAPL", "Apple Inc.")));
        when(mockStockMapper.toStocks(nyseResponse))
                .thenReturn(List.of(createStock("JPM", "JPMorgan Chase")));

        StockFilter filter = new StockFilter(List.of("United States"), List.of("NASDAQ", "NYSE"));

        List<Stock> result = marketDataAdapter.fetchStocks(filter)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(2);
        verify(mockClient).getStocks(eq(apiKey), eq("NASDAQ"), eq("United States"));
        verify(mockClient).getStocks(eq(apiKey), eq("NYSE"), eq("United States"));
    }

    @Test
    @DisplayName("Should deduplicate stocks across multiple API calls")
    void shouldDeduplicateStocksAcrossMultipleApiCalls() {
        var nasdaqResponse = new TwelveDataStockResponse(List.of(), "nasdaq");
        var nyseResponse = new TwelveDataStockResponse(List.of(), "nyse");
        Stock duplicateStock = createStock("AAPL", "Apple Inc.");

        when(mockClient.getStocks(eq(apiKey), eq("NASDAQ"), eq("United States")))
                .thenReturn(Uni.createFrom().item(nasdaqResponse));
        when(mockClient.getStocks(eq(apiKey), eq("NYSE"), eq("United States")))
                .thenReturn(Uni.createFrom().item(nyseResponse));
        when(mockStockMapper.toStocks(nasdaqResponse)).thenReturn(List.of(duplicateStock));
        when(mockStockMapper.toStocks(nyseResponse)).thenReturn(List.of(duplicateStock));

        StockFilter filter = new StockFilter(List.of("United States"), List.of("NASDAQ", "NYSE"));

        List<Stock> result = marketDataAdapter.fetchStocks(filter)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(1);
        verify(mockClient).getStocks(eq(apiKey), eq("NASDAQ"), eq("United States"));
        verify(mockClient).getStocks(eq(apiKey), eq("NYSE"), eq("United States"));
    }

    @Test
    @DisplayName("Should pass provider-side filters for single exchange and country")
    void shouldPassProviderSideFiltersForSingleExchangeAndCountry() {
        var apiResponse = createMockApiResponse();
        var expectedStocks = List.of(createStock("AAPL", "Apple Inc."));

        when(mockClient.getStocks(eq(apiKey), eq("NASDAQ"), eq("United States")))
                .thenReturn(Uni.createFrom().item(apiResponse));
        when(mockStockMapper.toStocks(apiResponse)).thenReturn(expectedStocks);

        StockFilter filter = new StockFilter(List.of("United States"), List.of("NASDAQ"));

        List<Stock> result = marketDataAdapter.fetchStocks(filter)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).hasSize(1);
        verify(mockClient).getStocks(eq(apiKey), eq("NASDAQ"), eq("United States"));
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
        List<Stock> stocks = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            stocks.add(createStock("TEST" + i, "Test Stock " + i));
        }
        return stocks;
    }

    private Stock createStock(String symbol, String name) {
        return new Stock(
                null,
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
                1L,
                0.0
        );
    }
}
