package com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockMapperTest {

    private StockMapper stockMapper;

    @BeforeEach
    void setUp() {
        stockMapper = Mappers.getMapper(StockMapper.class);
    }

    @Test
    void shouldMapStockToStockEntity() {
        // Given
        Stock stock = new Stock(
                1L,
                "AAPL",
                "Apple Inc.",
                "USD",
                "NASDAQ",
                "XNAS",
                "United States",
                "Common Stock",
                "BBG000B9XRY4",
                "ESXXXX",
                "US0378331005",
                "037833100",
                1L
        );

        // When
        StockEntity result = stockMapper.toStockEntity(stock);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSymbol()).isEqualTo("AAPL");
        assertThat(result.getName()).isEqualTo("Apple Inc.");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getExchange()).isEqualTo("NASDAQ");
        assertThat(result.getMicCode()).isEqualTo("XNAS");
        assertThat(result.getCountry()).isEqualTo("United States");
        assertThat(result.getType()).isEqualTo("Common Stock");
        assertThat(result.getFigiCode()).isEqualTo("BBG000B9XRY4");
        assertThat(result.getCfiCode()).isEqualTo("ESXXXX");
        assertThat(result.getIsin()).isEqualTo("US0378331005");
        assertThat(result.getCusip()).isEqualTo("037833100");
        assertThat(result.getDataVersion()).isEqualTo(1L);
    }

    @Test
    void shouldMapStockEntityToStock() {
        // Given
        StockEntity entity = new StockEntity();
        entity.setSymbol("MSFT");
        entity.setName("Microsoft Corporation");
        entity.setCurrency("USD");
        entity.setExchange("NASDAQ");
        entity.setMicCode("XNAS");
        entity.setCountry("United States");
        entity.setType("Common Stock");
        entity.setFigiCode("BBG000BPH459");
        entity.setCfiCode("ESXXXX");
        entity.setIsin("US5949181045");
        entity.setCusip("594918104");
        entity.setDataVersion(1L);
        entity.setIsActive(true);

        // When
        Stock result = stockMapper.toStock(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("MSFT");
        assertThat(result.name()).isEqualTo("Microsoft Corporation");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.exchange()).isEqualTo("NASDAQ");
        assertThat(result.micCode()).isEqualTo("XNAS");
        assertThat(result.country()).isEqualTo("United States");
        assertThat(result.type()).isEqualTo("Common Stock");
        assertThat(result.figiCode()).isEqualTo("BBG000BPH459");
        assertThat(result.cfiCode()).isEqualTo("ESXXXX");
        assertThat(result.isin()).isEqualTo("US5949181045");
        assertThat(result.cusip()).isEqualTo("594918104");
        assertThat(result.dataVersion()).isEqualTo(1L);
    }

    @Test
    void shouldMapStockListToStockEntityList() {
        // Given
        List<Stock> stocks = List.of(
                new Stock(1L, "AAPL", "Apple Inc.", "USD", "NASDAQ", "XNAS", "United States", 
                         "Common Stock", "BBG000B9XRY4", "ESXXXX", "US0378331005", "037833100", 1L),
                new Stock(2L, "MSFT", "Microsoft Corporation", "USD", "NASDAQ", "XNAS", "United States", 
                         "Common Stock", "BBG000BPH459", "ESXXXX", "US5949181045", "594918104", 1L)
        );

        // When
        List<StockEntity> result = stockMapper.toStockEntities(stocks);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getName()).isEqualTo("Apple Inc.");
        assertThat(result.get(0).getExchange()).isEqualTo("NASDAQ");
        
        assertThat(result.get(1).getSymbol()).isEqualTo("MSFT");
        assertThat(result.get(1).getName()).isEqualTo("Microsoft Corporation");
        assertThat(result.get(1).getExchange()).isEqualTo("NASDAQ");
        
        // Verify all entities have proper mapping
        result.forEach(entity -> {
            assertThat(entity.getSymbol()).isNotNull();
            assertThat(entity.getName()).isNotNull();
            assertThat(entity.getCurrency()).isEqualTo("USD");
            assertThat(entity.getCountry()).isEqualTo("United States");
            assertThat(entity.getDataVersion()).isEqualTo(1L);
        });
    }

    @Test
    void shouldMapStockEntityListToStockList() {
        // Given
        StockEntity entity1 = createStockEntity("GOOGL", "Alphabet Inc.");
        StockEntity entity2 = createStockEntity("TSLA", "Tesla Inc.");
        List<StockEntity> entities = List.of(entity1, entity2);

        // When
        List<Stock> result = stockMapper.toStocks(entities);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).symbol()).isEqualTo("GOOGL");
        assertThat(result.get(0).name()).isEqualTo("Alphabet Inc.");
        assertThat(result.get(0).exchange()).isEqualTo("NASDAQ");
        
        assertThat(result.get(1).symbol()).isEqualTo("TSLA");
        assertThat(result.get(1).name()).isEqualTo("Tesla Inc.");
        assertThat(result.get(1).exchange()).isEqualTo("NASDAQ");
        
        // Verify all stocks have proper mapping
        result.forEach(stock -> {
            assertThat(stock.symbol()).isNotNull();
            assertThat(stock.name()).isNotNull();
            assertThat(stock.currency()).isEqualTo("USD");
            assertThat(stock.country()).isEqualTo("United States");
            assertThat(stock.dataVersion()).isEqualTo(1L);
        });
    }

    @Test
    void shouldReturnEmptyListWhenMappingEmptyLists() {
        // Given
        List<Stock> emptyStockList = List.of();
        List<StockEntity> emptyEntityList = List.of();

        // When
        List<StockEntity> stockToEntityResult = stockMapper.toStockEntities(emptyStockList);
        List<Stock> entityToStockResult = stockMapper.toStocks(emptyEntityList);

        // Then
        assertThat(stockToEntityResult).isNotNull().isEmpty();
        assertThat(entityToStockResult).isNotNull().isEmpty();
    }

    @Test
    void shouldHandleMappingWithNullValues() {
        // Given
        Stock stockWithNulls = new Stock(
                3L,
                "TST",
                "Test Company",
                null, // currency is null
                null, // exchange is null
                null,
                null, // country is null
                null, // type is null
                null,
                null,
                null,
                null,
                1L
        );

        // When
        StockEntity entityResult = stockMapper.toStockEntity(stockWithNulls);

        // Then
        assertThat(entityResult).isNotNull();
        assertThat(entityResult.getSymbol()).isEqualTo("TST");
        assertThat(entityResult.getName()).isEqualTo("Test Company");
        assertThat(entityResult.getCurrency()).isNull();
        assertThat(entityResult.getExchange()).isNull();
        assertThat(entityResult.getCountry()).isNull();
        assertThat(entityResult.getType()).isNull();
        assertThat(entityResult.getDataVersion()).isEqualTo(1L);
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
