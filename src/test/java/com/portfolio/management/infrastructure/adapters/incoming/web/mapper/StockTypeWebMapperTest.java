package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.StockTypeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockTypeWebMapperTest {

    private StockTypeWebMapper stockTypeWebMapper;

    @BeforeEach
    void setUp() {
        stockTypeWebMapper = Mappers.getMapper(StockTypeWebMapper.class);
    }

    @Test
    void shouldMapStockTypeToResponseDto() {
        // Given
        StockType stockType = StockType.of(1L, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true);

        // When
        StockTypeResponse.StockTypeDto result = stockTypeWebMapper.toStockTypeDto(stockType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("ETF");
        assertThat(result.name()).isEqualTo("Exchange Traded Fund");
        assertThat(result.description()).isEqualTo("Investment fund traded on exchanges");
        assertThat(result.active()).isTrue(); // mapped from isActive
    }

    @Test
    void shouldMapStockTypeListToResponseDtoList() {
        // Given
        List<StockType> stockTypes = List.of(
                StockType.of(1L, "COMMON_STOCK", "Common Stock", "Common stock shares", true),
                StockType.of(2L, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true),
                StockType.of(3L, "REIT", "Real Estate Investment Trust", "Real estate investment trust", false)
        );

        // When
        List<StockTypeResponse.StockTypeDto> result = stockTypeWebMapper.toStockTypeDtoList(stockTypes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).code()).isEqualTo("COMMON_STOCK");
        assertThat(result.get(0).name()).isEqualTo("Common Stock");
        assertThat(result.get(0).active()).isTrue();
        
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).code()).isEqualTo("ETF");
        assertThat(result.get(1).name()).isEqualTo("Exchange Traded Fund");
        assertThat(result.get(1).active()).isTrue();
        
        assertThat(result.get(2).id()).isEqualTo(3L);
        assertThat(result.get(2).code()).isEqualTo("REIT");
        assertThat(result.get(2).name()).isEqualTo("Real Estate Investment Trust");
        assertThat(result.get(2).active()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenMappingEmptyStockTypeList() {
        // Given
        List<StockType> emptyStockTypeList = List.of();

        // When
        List<StockTypeResponse.StockTypeDto> result = stockTypeWebMapper.toStockTypeDtoList(emptyStockTypeList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapStockTypeListToStockTypeResponse() {
        // Given
        List<StockType> stockTypes = List.of(
                StockType.of(1L, "COMMON_STOCK", "Common Stock", "Common stock shares", true),
                StockType.of(2L, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true)
        );

        // When
        StockTypeResponse result = stockTypeWebMapper.toStockTypeResponse(stockTypes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.stockTypes()).hasSize(2);
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.stockTypes().get(0).code()).isEqualTo("COMMON_STOCK");
        assertThat(result.stockTypes().get(1).code()).isEqualTo("ETF");
    }

    @Test
    void shouldMapSingleStockTypeToStockTypeResponse() {
        // Given
        StockType stockType = StockType.of(1L, "PREFERRED_STOCK", "Preferred Stock", "Preferred stock shares", true);

        // When
        StockTypeResponse result = stockTypeWebMapper.toStockTypeResponse(stockType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.stockTypes()).hasSize(1);
        assertThat(result.count()).isEqualTo(1);
        assertThat(result.stockTypes().get(0).id()).isEqualTo(1L);
        assertThat(result.stockTypes().get(0).code()).isEqualTo("PREFERRED_STOCK");
        assertThat(result.stockTypes().get(0).name()).isEqualTo("Preferred Stock");
        assertThat(result.stockTypes().get(0).description()).isEqualTo("Preferred stock shares");
        assertThat(result.stockTypes().get(0).active()).isTrue();
    }

    @Test
    void shouldHandleMappingStockTypeWithNullDescription() {
        // Given
        StockType stockTypeWithNullDescription = new StockType(1L, "TST", "Test Stock Type", null, true);

        // When
        StockTypeResponse.StockTypeDto result = stockTypeWebMapper.toStockTypeDto(stockTypeWithNullDescription);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("TST");
        assertThat(result.name()).isEqualTo("Test Stock Type");
        assertThat(result.description()).isNull();
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldMapInactiveStockTypeCorrectly() {
        // Given
        StockType inactiveStockType = StockType.of(1L, "INACTIVE", "Inactive Type", "Inactive stock type", false);

        // When
        StockTypeResponse.StockTypeDto result = stockTypeWebMapper.toStockTypeDto(inactiveStockType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("INACTIVE");
        assertThat(result.name()).isEqualTo("Inactive Type");
        assertThat(result.description()).isEqualTo("Inactive stock type");
        assertThat(result.active()).isFalse(); // mapped from isActive = false
    }
}
