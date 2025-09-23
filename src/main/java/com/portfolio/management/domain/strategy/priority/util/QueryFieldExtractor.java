package com.portfolio.management.domain.strategy.priority.util;

import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.strategy.priority.SearchField;

/**
 * Utility class to extract search terms from queries based on search fields
 */
public class QueryFieldExtractor {

    /**
     * Extract the appropriate search term from an advanced query based on the search field
     *
     * @param query       The advanced search query
     * @param searchField The field to extract
     * @return The search term for the specified field, or null if not present
     */
    public static String extractSearchTerm(GetSuggestionsAdvancedUseCase.Query query, SearchField searchField) {
        return switch (searchField) {
            case SYMBOL -> query.symbol();
            case NAME -> query.companyName();
            case EXCHANGE -> query.exchange();
            case COUNTRY -> query.country();
            case CURRENCY -> query.currency();
            case TYPE -> null; // Type not available in current query
        };
    }
}
