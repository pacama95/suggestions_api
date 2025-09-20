package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.SuggestionsResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

/**
 * MapStruct mapper for converting between TickerSuggestion and TickerSuggestionDto
 */
@Mapper(componentModel = JAKARTA_CDI)
public interface StockMapper {

    /**
     * Maps TickerSuggestion domain model to TickerSuggestionDto
     */
    SuggestionsResponse.TickerSuggestionDto toTickerSuggestionDto(Stock tickerSuggestion);

    /**
     * Maps list of TickerSuggestion to list of TickerSuggestionDto
     */
    List<SuggestionsResponse.TickerSuggestionDto> toTickerSuggestionDtoList(List<Stock> tickerSuggestions);
}


