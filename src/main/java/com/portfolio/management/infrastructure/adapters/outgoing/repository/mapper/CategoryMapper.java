package com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.CurrencyEntity;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.ExchangeEntity;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between category entities and domain models
 */
@Mapper(componentModel = "jakarta")
public interface CategoryMapper {
    
    // StockType mappings
    @Mapping(target = "isActive", source = "isActive")
    StockType toStockType(StockTypeEntity entity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", source = "isActive")
    StockTypeEntity toStockTypeEntity(StockType stockType);
    
    // Currency mappings
    @Mapping(target = "isActive", source = "isActive")
    Currency toCurrency(CurrencyEntity entity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", source = "isActive")
    CurrencyEntity toCurrencyEntity(Currency currency);
    
    // Exchange mappings
    @Mapping(target = "isActive", source = "isActive")
    Exchange toExchange(ExchangeEntity entity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", source = "isActive")
    ExchangeEntity toExchangeEntity(Exchange exchange);
}
