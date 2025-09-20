package com.portfolio.management.infrastructure.adapters.outgoing.client.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
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
    void shouldMapTwelveDataResponseToStockList() {
        // Given
        var apiStock1 = new TwelveDataStockResponse.TwelveDataStock(
                "aapl", // lowercase symbol
                "  Apple Inc.  ", // name with spaces
                "USD",
                "NASDAQ",
                "XNAS",
                "United States",
                "Common Stock",
                "BBG000B9XRY4",
                "ESXXXX",
                "US0378331005",
                "037833100"
        );
        
        var apiStock2 = new TwelveDataStockResponse.TwelveDataStock(
                "msft",
                "Microsoft Corporation",
                "USD",
                "NASDAQ",
                "XNAS",
                "United States",
                "Common Stock",
                "BBG000BPH459",
                "ESXXXX",
                "US5949181045",
                "594918104"
        );
        
        var response = new TwelveDataStockResponse(
                List.of(apiStock1, apiStock2),
                "ok"
        );

        // When
        List<Stock> result = stockMapper.toStocks(response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).symbol()).isEqualTo("AAPL"); // cleaned and uppercased
        assertThat(result.get(0).name()).isEqualTo("Apple Inc."); // cleaned (trimmed)
        assertThat(result.get(0).currency()).isEqualTo("USD");
        assertThat(result.get(0).exchange()).isEqualTo("NASDAQ");
        assertThat(result.get(0).dataVersion()).isEqualTo(1L); // default value
        
        assertThat(result.get(1).symbol()).isEqualTo("MSFT");
        assertThat(result.get(1).name()).isEqualTo("Microsoft Corporation");
        assertThat(result.get(1).currency()).isEqualTo("USD");
    }

    @Test
    void shouldMapTwelveDataStockToDomainStock() {
        // Given
        var apiStock = new TwelveDataStockResponse.TwelveDataStock(
                "googl",
                "Alphabet Inc.",
                "USD",
                "NASDAQ",
                "XNAS",
                "United States",
                "Common Stock",
                "BBG009S39JX6",
                "ESXXXX",
                "US02079K3059",
                "02079K305"
        );

        // When
        Stock result = stockMapper.toStock(apiStock);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("GOOGL"); // cleaned symbol
        assertThat(result.name()).isEqualTo("Alphabet Inc."); // cleaned name
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.exchange()).isEqualTo("NASDAQ");
        assertThat(result.micCode()).isEqualTo("XNAS");
        assertThat(result.country()).isEqualTo("United States");
        assertThat(result.type()).isEqualTo("Common Stock");
        assertThat(result.figiCode()).isEqualTo("BBG009S39JX6");
        assertThat(result.cfiCode()).isEqualTo("ESXXXX");
        assertThat(result.isin()).isEqualTo("US02079K3059");
        assertThat(result.cusip()).isEqualTo("02079K305");
        assertThat(result.dataVersion()).isEqualTo(1L);
    }

    @Test
    void shouldFilterOutInvalidStocksFromResponse() {
        // Given
        var validStock = new TwelveDataStockResponse.TwelveDataStock(
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
                "037833100"
        );
        
        var invalidStock1 = new TwelveDataStockResponse.TwelveDataStock(
                null, // null symbol - invalid
                "Some Company",
                "USD",
                "NYSE",
                "XNYS",
                "United States",
                "Common Stock",
                null,
                null,
                null,
                null
        );
        
        var invalidStock2 = new TwelveDataStockResponse.TwelveDataStock(
                "TST",
                "", // empty name - invalid
                "USD",
                "NYSE",
                "XNYS",
                "United States",
                "Common Stock",
                null,
                null,
                null,
                null
        );
        
        var response = new TwelveDataStockResponse(
                List.of(validStock, invalidStock1, invalidStock2),
                "ok"
        );

        // When
        List<Stock> result = stockMapper.toStocks(response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Only the valid stock should be included
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
        assertThat(result.get(0).name()).isEqualTo("Apple Inc.");
    }

    @Test
    void shouldReturnEmptyListWhenApiResponseIsEmpty() {
        // Given
        var response = new TwelveDataStockResponse(
                List.of(),
                "ok"
        );

        // When
        List<Stock> result = stockMapper.toStocks(response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenApiResponseHasNoData() {
        // Given
        var response = new TwelveDataStockResponse(
                null, // no data
                "ok"
        );

        // When
        List<Stock> result = stockMapper.toStocks(response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenApiResponseIsNull() {
        // Given
        TwelveDataStockResponse response = null;

        // When
        List<Stock> result = stockMapper.toStocks(response);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleStockWithNullOptionalFields() {
        // Given
        var apiStock = new TwelveDataStockResponse.TwelveDataStock(
                "TST",
                "Test Company",
                null, // null currency
                null, // null exchange
                null, // null micCode
                null, // null country
                null, // null type
                null, // null figiCode
                null, // null cfiCode
                null, // null isin
                null  // null cusip
        );

        // When
        Stock result = stockMapper.toStock(apiStock);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("TST");
        assertThat(result.name()).isEqualTo("Test Company");
        assertThat(result.currency()).isNull();
        assertThat(result.exchange()).isNull();
        assertThat(result.micCode()).isNull();
        assertThat(result.country()).isNull();
        assertThat(result.type()).isNull();
        assertThat(result.figiCode()).isNull();
        assertThat(result.cfiCode()).isNull();
        assertThat(result.isin()).isNull();
        assertThat(result.cusip()).isNull();
        assertThat(result.dataVersion()).isEqualTo(1L);
    }
}
