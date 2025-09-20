package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.model.Stock;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Use case for getting ticker suggestions based on search criteria
 */
public interface GetSuggestionsAdvancedUseCase {

    Uni<Result> execute(Query query);


    record Query(String symbol, String companyName, String exchange, String country, String currency, int limit) {

        public Query {
            if (limit <= 0 || limit > 100) {
                throw new IllegalArgumentException("Limit must be between 1 and 100");
            }
        }

        public Query(String symbol,
                     String companyName,
                     String exchange,
                     String country,
                     String currency) {
            this(symbol, companyName, exchange, country, currency, 10); // Default limit
        }

        /**
         * Check if at least one search parameter is provided
         */
        public boolean hasSearchCriteria() {
            return isNotBlank(symbol) || isNotBlank(companyName) || isNotBlank(exchange) ||
                    isNotBlank(country) || isNotBlank(currency);
        }

        private boolean isNotBlank(String str) {
            return str != null && !str.trim().isEmpty();
        }

        /**
         * Get a description of the search criteria for logging/response purposes
         */
        public String getSearchDescription() {
            var description = new StringBuilder("Advanced search");
            if (isNotBlank(symbol)) description.append(" symbol:").append(symbol);
            if (isNotBlank(companyName)) description.append(" company:").append(companyName);
            if (isNotBlank(exchange)) description.append(" exchange:").append(exchange);
            if (isNotBlank(country)) description.append(" country:").append(country);
            if (isNotBlank(currency)) description.append(" currency:").append(currency);
            return description.toString();
        }
    }

    sealed interface Result {

        record Success(List<Stock> suggestions, String query, int count) implements Result {

            public Success {
                if (suggestions == null) {
                    throw new IllegalArgumentException("Suggestions cannot be null");
                }
                count = suggestions.size();
            }
        }

        record ValidationError(Errors errors) implements Result {
        }

        record SystemError(Errors errors) implements Result {
        }
    }
}