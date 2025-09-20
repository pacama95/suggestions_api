package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.StockTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting between StockType domain models and web DTOs
 */
@Mapper(componentModel = "jakarta")
public interface StockTypeWebMapper {
    
    @Mapping(target = "active", source = "isActive")
    StockTypeResponse.StockTypeDto toStockTypeDto(StockType stockType);
    
    List<StockTypeResponse.StockTypeDto> toStockTypeDtoList(List<StockType> stockTypes);
    
    default StockTypeResponse toStockTypeResponse(List<StockType> stockTypes) {
        List<StockTypeResponse.StockTypeDto> dtoList = toStockTypeDtoList(stockTypes);
        return new StockTypeResponse(dtoList, dtoList.size());
    }
    
    default StockTypeResponse toStockTypeResponse(StockType stockType) {
        StockTypeResponse.StockTypeDto dto = toStockTypeDto(stockType);
        return new StockTypeResponse(dto);
    }
}
