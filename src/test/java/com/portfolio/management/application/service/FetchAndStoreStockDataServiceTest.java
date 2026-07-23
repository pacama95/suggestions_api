package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import com.portfolio.management.domain.port.outgoing.MarketDataPort;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.domain.model.StocksBatchProcessingResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchAndStoreStockDataServiceTest {

    @Mock
    MarketDataPort marketDataPort;

    @Mock
    StockPort stockPort;

    private FetchAndStoreStockDataService stockDataService;

    @BeforeEach
    void setUp() {
        stockDataService = new FetchAndStoreStockDataService(
                marketDataPort,
                stockPort,
                Optional.of(List.of("United States")),
                Optional.of(List.of("NASDAQ"))
        );
    }

    @Test
    @DisplayName("Should use request filter values when provided")
    void shouldUseRequestFilterValuesWhenProvided() {
        StockFilter requestFilter = new StockFilter(List.of("Germany"), List.of("XETR"));
        StockFilter expectedFilter = new StockFilter(List.of("Germany"), List.of("XETR"));

        assertThat(stockDataService.mergeWithDefaults(requestFilter)).isEqualTo(expectedFilter);
    }

    @Test
    @DisplayName("Should fall back to configured defaults when request filter is empty")
    void shouldFallBackToConfiguredDefaultsWhenRequestFilterIsEmpty() {
        StockFilter requestFilter = StockFilter.empty();
        StockFilter expectedFilter = new StockFilter(List.of("United States"), List.of("NASDAQ"));

        assertThat(stockDataService.mergeWithDefaults(requestFilter)).isEqualTo(expectedFilter);
    }

    @Test
    @DisplayName("Should fetch and store stocks successfully")
    void shouldFetchAndStoreStocksSuccessfully() {
        List<Stock> fetchedStocks = List.of(createStock("AAPL"), createStock("MSFT"));
        StockFilter requestFilter = new StockFilter(List.of("United States"), List.of("NASDAQ"));

        when(marketDataPort.fetchStocks(any(StockFilter.class)))
                .thenReturn(Uni.createFrom().item(fetchedStocks));
        when(stockPort.deleteAll()).thenReturn(Uni.createFrom().item(2L));
        when(stockPort.saveBatch(any())).thenReturn(Uni.createFrom().item(new StocksBatchProcessingResult(2, 0)));
        when(stockPort.analyzeTable()).thenReturn(Uni.createFrom().voidItem());

        FetchAndStoreStockDataUseCase.Result result = stockDataService.fetchAndStoreStocks(requestFilter)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertThat(result).isInstanceOf(FetchAndStoreStockDataUseCase.Result.Success.class);
        FetchAndStoreStockDataUseCase.Result.Success success = (FetchAndStoreStockDataUseCase.Result.Success) result;
        assertThat(success.success()).isTrue();
        assertThat(success.recordsProcessed()).isEqualTo(2);

        ArgumentCaptor<StockFilter> filterCaptor = ArgumentCaptor.forClass(StockFilter.class);
        verify(marketDataPort).fetchStocks(filterCaptor.capture());
        assertThat(filterCaptor.getValue()).isEqualTo(requestFilter);
        verify(stockPort).deleteAll();
        verify(stockPort).saveBatch(fetchedStocks);
        verify(stockPort).analyzeTable();
    }

    @Test
    @DisplayName("Should return success with zero records when no stocks are fetched")
    void shouldReturnSuccessWithZeroRecordsWhenNoStocksFetched() {
        when(marketDataPort.fetchStocks(any(StockFilter.class)))
                .thenReturn(Uni.createFrom().item(List.of()));

        FetchAndStoreStockDataUseCase.Result result = stockDataService.fetchAndStoreStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertThat(result).isInstanceOf(FetchAndStoreStockDataUseCase.Result.Success.class);
        FetchAndStoreStockDataUseCase.Result.Success success = (FetchAndStoreStockDataUseCase.Result.Success) result;
        assertThat(success.success()).isFalse();
        assertThat(success.recordsProcessed()).isZero();
    }

    @Test
    @DisplayName("Should return error when market data fetch fails")
    void shouldReturnErrorWhenMarketDataFetchFails() {
        when(marketDataPort.fetchStocks(any(StockFilter.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("API unavailable")));

        FetchAndStoreStockDataUseCase.Result result = stockDataService.fetchAndStoreStocks(StockFilter.empty())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertThat(result).isInstanceOf(FetchAndStoreStockDataUseCase.Result.Error.class);
        assertThat(((FetchAndStoreStockDataUseCase.Result.Error) result).message())
                .contains("API unavailable");
    }

    private Stock createStock(String symbol) {
        return Stock.of(symbol, symbol + " Inc.", "USD", "NASDAQ", "XNAS",
                "United States", "Common Stock", null, null, null, null, 1L);
    }
}
