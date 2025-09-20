package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.CurrencyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting between Currency domain models and web DTOs
 */
@Mapper(componentModel = "jakarta")
public interface CurrencyWebMapper {
    
    @Mapping(target = "active", source = "isActive")
    CurrencyResponse.CurrencyDto toCurrencyDto(Currency currency);
    
    List<CurrencyResponse.CurrencyDto> toCurrencyDtoList(List<Currency> currencies);
    
    default CurrencyResponse toCurrencyResponse(List<Currency> currencies) {
        List<CurrencyResponse.CurrencyDto> dtoList = toCurrencyDtoList(currencies);
        return new CurrencyResponse(dtoList, dtoList.size());
    }
    
    default CurrencyResponse toCurrencyResponse(Currency currency) {
        CurrencyResponse.CurrencyDto dto = toCurrencyDto(currency);
        return new CurrencyResponse(dto);
    }
}
