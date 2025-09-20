package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.SuggestionsResponse;
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
    void shouldMapStockToTickerSuggestionDto() {
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
        SuggestionsResponse.TickerSuggestionDto result = stockMapper.toTickerSuggestionDto(stock);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.name()).isEqualTo("Apple Inc.");
        assertThat(result.exchange()).isEqualTo("NASDAQ");
        assertThat(result.type()).isEqualTo("Common Stock");
        assertThat(result.country()).isEqualTo("United States");
        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void shouldMapStockListToTickerSuggestionDtoList() {
        // Given
        List<Stock> stocks = List.of(
                new Stock(1L, "AAPL", "Apple Inc.", "USD", "NASDAQ", "XNAS", "United States", 
                         "Common Stock", "BBG000B9XRY4", "ESXXXX", "US0378331005", "037833100", 1L),
                new Stock(2L, "MSFT", "Microsoft Corporation", "USD", "NASDAQ", "XNAS", "United States", 
                         "Common Stock", "BBG000BPH459", "ESXXXX", "US5949181045", "594918104", 1L),
                new Stock(3L, "GOOGL", "Alphabet Inc.", "USD", "NASDAQ", "XNAS", "United States", 
                         "Common Stock", "BBG009S39JX6", "ESXXXX", "US02079K3059", "02079K305", 1L)
        );

        // When
        List<SuggestionsResponse.TickerSuggestionDto> result = stockMapper.toTickerSuggestionDtoList(stocks);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
        assertThat(result.get(0).name()).isEqualTo("Apple Inc.");
        assertThat(result.get(0).exchange()).isEqualTo("NASDAQ");
        
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).symbol()).isEqualTo("MSFT");
        assertThat(result.get(1).name()).isEqualTo("Microsoft Corporation");
        
        assertThat(result.get(2).id()).isEqualTo(3L);
        assertThat(result.get(2).symbol()).isEqualTo("GOOGL");
        assertThat(result.get(2).name()).isEqualTo("Alphabet Inc.");
        
        // Verify all DTOs have proper mapping
        result.forEach(dto -> {
            assertThat(dto.id()).isNotNull();
            assertThat(dto.symbol()).isNotNull();
            assertThat(dto.name()).isNotNull();
            assertThat(dto.exchange()).isEqualTo("NASDAQ");
            assertThat(dto.country()).isEqualTo("United States");
            assertThat(dto.currency()).isEqualTo("USD");
            assertThat(dto.type()).isEqualTo("Common Stock");
        });
    }

    @Test
    void shouldReturnEmptyListWhenMappingEmptyStockList() {
        // Given
        List<Stock> emptyStockList = List.of();

        // When
        List<SuggestionsResponse.TickerSuggestionDto> result = stockMapper.toTickerSuggestionDtoList(emptyStockList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleMappingStockWithNullValues() {
        // Given
        Stock stockWithNulls = new Stock(
                4L,
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
        SuggestionsResponse.TickerSuggestionDto result = stockMapper.toTickerSuggestionDto(stockWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.symbol()).isEqualTo("TST");
        assertThat(result.name()).isEqualTo("Test Company");
        assertThat(result.currency()).isNull();
        assertThat(result.exchange()).isNull();
        assertThat(result.country()).isNull();
        assertThat(result.type()).isNull();
    }
}
