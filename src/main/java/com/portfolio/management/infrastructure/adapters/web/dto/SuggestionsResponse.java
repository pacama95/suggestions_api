package com.portfolio.management.infrastructure.adapters.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Response DTO for ticker suggestions
 */
@RegisterForReflection
public record SuggestionsResponse(
    List<TickerSuggestionDto> suggestions,
    String query,
    int count
) {
    
    @RegisterForReflection
    public record TickerSuggestionDto(
        String symbol,
        String name,
        String exchange,
        String type,
        String region,
        String marketCap,
        String currency
    ) {
    }
}
