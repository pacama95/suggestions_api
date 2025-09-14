package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.TickerSuggestion;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

/**
 * Port for accessing ticker suggestion data
 */
public interface SuggestionRepository {
    
    /**
     * Search for ticker suggestions matching the given query
     */
    Uni<List<TickerSuggestion>> findByQuery(String query, int limit);
    
    /**
     * Find a specific ticker by its symbol
     */
    Uni<Optional<TickerSuggestion>> findBySymbol(String symbol);
    
    /**
     * Get all available tickers with pagination
     */
    Uni<List<TickerSuggestion>> findAll(int offset, int limit);
}
