package com.portfolio.management.infrastructure.adapters.outgoing.client.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

/**
 * MapStruct mapper for converting TwelveData API responses to Stock domain objects
 */
@Mapper(componentModel = JAKARTA_CDI)
public interface StockMapper {

    /**
     * Maps TwelveDataStockResponse to List of Stock domain objects
     * Filters out invalid stocks and stocks with restricted access
     */
    default List<Stock> toStocks(TwelveDataStockResponse twelveDataStockResponse) {
        if (twelveDataStockResponse == null || twelveDataStockResponse.data() == null) {
            return List.of();
        }
        
        return twelveDataStockResponse.data().stream()
            .filter(TwelveDataStockResponse.TwelveDataStock::isValid)
            .map(this::toStock)
            .collect(Collectors.toList());
    }

    /**
     * Maps individual TwelveDataStock to Stock domain object
     * Uses cleaned symbol and name values and sets default dataVersion
     * ID is set to null as external API doesn't provide database IDs
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "symbol", expression = "java(twelveDataStock.getCleanedSymbol())")
    @Mapping(target = "name", expression = "java(twelveDataStock.getCleanedName())")
    @Mapping(target = "dataVersion", constant = "1L")
    Stock toStock(TwelveDataStockResponse.TwelveDataStock twelveDataStock);

}
