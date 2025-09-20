package com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

/**
 * MapStruct mapper for converting between StockEntity and Stock domain model
 */
@Mapper(componentModel = JAKARTA_CDI)
public interface StockMapper {

    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StockEntity toStockEntity(Stock stock);

    List<StockEntity> toStockEntities(List<Stock> stocks);

    Stock toStock(StockEntity stock);

    List<Stock> toStocks(List<StockEntity> stockEntities);
}


