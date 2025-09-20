package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StocksBatchProcessingResult;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseStockRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.StockMapper;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPersistenceAdapterTest {

    @Mock
    private DatabaseStockRepository mockDatabaseRepository;

    @Mock
    private StockMapper mockStockMapper;

    private StockPersistenceAdapter stockPersistenceAdapter;

    @BeforeEach
    void setUp() {
        stockPersistenceAdapter = new StockPersistenceAdapter(mockStockMapper, mockDatabaseRepository);
    }

    @Test
    void shouldReturnStocksWhenValidQueryProvided() {
        // Given
        String query = "AAPL";
        int limit = 10;
        var stockEntities = List.of(createStockEntity("AAPL", "Apple Inc."));
        var expectedStocks = List.of(createStock("AAPL", "Apple Inc."));

        when(mockDatabaseRepository.findActiveByQuery(query, limit)).thenReturn(Uni.createFrom().item(stockEntities));
        when(mockStockMapper.toStocks(stockEntities)).thenReturn(expectedStocks);

        // When
        List<Stock> result = stockPersistenceAdapter.findActiveByQuery(query, limit)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
        assertThat(result.get(0).name()).isEqualTo("Apple Inc.");

        verify(mockDatabaseRepository).findActiveByQuery(eq(query), eq(limit));
        verify(mockStockMapper).toStocks(eq(stockEntities));
    }

    @Test
    void shouldReturnStockWhenValidSymbolProvided() {
        // Given
        String symbol = "MSFT";
        var stockEntity = createStockEntity("MSFT", "Microsoft Corporation");
        var expectedStock = createStock("MSFT", "Microsoft Corporation");

        when(mockDatabaseRepository.findActiveBySymbol(symbol)).thenReturn(Uni.createFrom().item(Optional.of(stockEntity)));
        when(mockStockMapper.toStock(stockEntity)).thenReturn(expectedStock);

        // When
        Stock result = stockPersistenceAdapter.findActiveBySymbol(symbol)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("MSFT");
        assertThat(result.name()).isEqualTo("Microsoft Corporation");

        verify(mockDatabaseRepository).findActiveBySymbol(eq(symbol));
        verify(mockStockMapper).toStock(eq(stockEntity));
    }

    @Test
    void shouldReturnNullWhenSymbolNotFound() {
        // Given
        String symbol = "NONEXISTENT";

        when(mockDatabaseRepository.findActiveBySymbol(symbol)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        Stock result = stockPersistenceAdapter.findActiveBySymbol(symbol)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNull();

        verify(mockDatabaseRepository).findActiveBySymbol(eq(symbol));
    }

    @Test
    void shouldReturnStocksWhenAdvancedSearchWithMultipleCriteria() {
        // Given
        String symbol = "AAPL";
        String companyName = "Apple";
        String exchange = "NASDAQ";
        String country = "United States";
        String currency = "USD";
        int limit = 25;
        var stockEntities = List.of(createStockEntity("AAPL", "Apple Inc."));
        var expectedStocks = List.of(createStock("AAPL", "Apple Inc."));

        when(mockDatabaseRepository.findByAdvancedSearch(symbol, companyName, exchange, country, currency, limit))
                .thenReturn(Uni.createFrom().item(stockEntities));
        when(mockStockMapper.toStocks(stockEntities)).thenReturn(expectedStocks);

        // When
        List<Stock> result = stockPersistenceAdapter.findByAdvancedSearch(symbol, companyName, exchange, country, currency, limit)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");

        verify(mockDatabaseRepository).findByAdvancedSearch(eq(symbol), eq(companyName), eq(exchange), eq(country), eq(currency), eq(limit));
        verify(mockStockMapper).toStocks(eq(stockEntities));
    }

    @Test
    void shouldSuccessfullySaveStock() {
        // Given
        var stock = createStock("GOOGL", "Alphabet Inc.");
        var stockEntity = createStockEntity("GOOGL", "Alphabet Inc.");
        var persistedEntity = createStockEntity("GOOGL", "Alphabet Inc.");

        when(mockStockMapper.toStockEntity(stock)).thenReturn(stockEntity);
        when(mockDatabaseRepository.persistAndFlush(stockEntity)).thenReturn(Uni.createFrom().item(persistedEntity));
        when(mockStockMapper.toStock(persistedEntity)).thenReturn(stock);

        // When
        Stock result = stockPersistenceAdapter.save(stock)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("GOOGL");
        assertThat(result.name()).isEqualTo("Alphabet Inc.");

        verify(mockStockMapper).toStockEntity(eq(stock));
        verify(mockDatabaseRepository).persistAndFlush(eq(stockEntity));
        verify(mockStockMapper).toStock(eq(persistedEntity));
    }

    @Test
    void shouldSuccessfullySaveStockList() {
        // Given
        var stocks = List.of(
                createStock("AAPL", "Apple Inc."),
                createStock("MSFT", "Microsoft Corporation")
        );
        var stockEntities = List.of(
                createStockEntity("AAPL", "Apple Inc."),
                createStockEntity("MSFT", "Microsoft Corporation")
        );

        when(mockStockMapper.toStockEntity(any(Stock.class))).thenReturn(createStockEntity("TEST", "Test"));
        when(mockDatabaseRepository.persistBatch(anyList())).thenReturn(Uni.createFrom().item(stockEntities));

        // When
        StocksBatchProcessingResult result = stockPersistenceAdapter.saveBatch(stocks)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.success()).isEqualTo(2);
        assertThat(result.errors()).isEqualTo(0);

        verify(mockDatabaseRepository).persistBatch(anyList());
    }

    @Test
    void shouldReturnActiveStockCount() {
        // Given
        Long expectedCount = 1500L;

        when(mockDatabaseRepository.countActive()).thenReturn(Uni.createFrom().item(expectedCount));

        // When
        Long result = stockPersistenceAdapter.countActive()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isEqualTo(expectedCount);

        verify(mockDatabaseRepository).countActive();
    }

    @Test
    void shouldDeleteAllStocks() {
        // Given
        Long expectedDeletedCount = 100L;

        when(mockDatabaseRepository.clearAll()).thenReturn(Uni.createFrom().item(expectedDeletedCount));

        // When
        Long result = stockPersistenceAdapter.deleteAll()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isEqualTo(expectedDeletedCount);

        verify(mockDatabaseRepository).clearAll();
    }

    @Test
    void shouldHandleErrorWhenRepositorySearchFails() {
        // Given
        String query = "TEST";
        int limit = 10;

        when(mockDatabaseRepository.findActiveByQuery(query, limit))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        Throwable exception = stockPersistenceAdapter.findActiveByQuery(query, limit)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Database error");

        verify(mockDatabaseRepository).findActiveByQuery(eq(query), eq(limit));
    }

    @Test
    void shouldHandleErrorWhenRepositorySaveFails() {
        // Given
        var stock = createStock("FAIL", "Failing Stock");
        var stockEntity = createStockEntity("FAIL", "Failing Stock");

        when(mockStockMapper.toStockEntity(stock)).thenReturn(stockEntity);
        when(mockDatabaseRepository.persistAndFlush(stockEntity))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Save failed")));

        // When
        Throwable exception = stockPersistenceAdapter.save(stock)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertFailed()
                .getFailure();

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("Save failed");

        verify(mockStockMapper).toStockEntity(eq(stock));
        verify(mockDatabaseRepository).persistAndFlush(eq(stockEntity));
    }

    @Test
    void shouldFallbackToIndividualSavesWhenBatchSaveFails() {
        // Given
        var stocks = List.of(createStock("AAPL", "Apple Inc."));
        var stockEntity = createStockEntity("AAPL", "Apple Inc.");

        when(mockStockMapper.toStockEntity(any(Stock.class))).thenReturn(stockEntity);
        when(mockDatabaseRepository.persistBatch(anyList()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Batch save failed")));
        when(mockDatabaseRepository.persistAndFlush(any(StockEntity.class)))
                .thenReturn(Uni.createFrom().item(stockEntity));
        when(mockStockMapper.toStock(stockEntity)).thenReturn(createStock("AAPL", "Apple Inc."));

        // When
        StocksBatchProcessingResult result = stockPersistenceAdapter.saveBatch(stocks)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.success()).isEqualTo(1);
        assertThat(result.errors()).isEqualTo(0);

        verify(mockDatabaseRepository).persistBatch(anyList());
        verify(mockDatabaseRepository).persistAndFlush(any(StockEntity.class));
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

    private StockEntity createStockEntity(String symbol, String name) {
        StockEntity entity = new StockEntity();
        entity.setSymbol(symbol);
        entity.setName(name);
        entity.setCurrency("USD");
        entity.setExchange("NASDAQ");
        entity.setMicCode("XNAS");
        entity.setCountry("United States");
        entity.setType("Common Stock");
        entity.setFigiCode("BBG123456789");
        entity.setCfiCode("ESXXXX");
        entity.setIsin("US1234567890");
        entity.setCusip("123456789");
        entity.setDataVersion(1L);
        entity.setIsActive(true);
        return entity;
    }
}
