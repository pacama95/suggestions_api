package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ExchangeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting between Exchange domain models and web DTOs
 */
@Mapper(componentModel = "jakarta")
public interface ExchangeWebMapper {
    
    @Mapping(target = "active", source = "isActive")
    ExchangeResponse.ExchangeDto toExchangeDto(Exchange exchange);
    
    List<ExchangeResponse.ExchangeDto> toExchangeDtoList(List<Exchange> exchanges);
    
    default ExchangeResponse toExchangeResponse(List<Exchange> exchanges) {
        List<ExchangeResponse.ExchangeDto> dtoList = toExchangeDtoList(exchanges);
        return new ExchangeResponse(dtoList, dtoList.size());
    }
    
    default ExchangeResponse toExchangeResponse(Exchange exchange) {
        ExchangeResponse.ExchangeDto dto = toExchangeDto(exchange);
        return new ExchangeResponse(dto);
    }
}
