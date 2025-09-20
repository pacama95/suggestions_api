package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.domain.model.Errors;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Use case for retrieving currency information
 */
public interface GetCurrenciesUseCase {
    
    Uni<Result> execute(Query query);
    
    record Query(String searchTerm, String countryCode, boolean majorOnly) {
        
        public Query() {
            this(null, null, false);
        }
        
        public Query(String searchTerm) {
            this(searchTerm, null, false);
        }
        
        public static Query majorCurrenciesOnly() {
            return new Query(null, null, true);
        }
        
        public static Query byCountryCode(String countryCode) {
            return new Query(null, countryCode, false);
        }
        
        public boolean isSearchQuery() {
            return searchTerm != null && !searchTerm.trim().isEmpty();
        }
        
        public boolean isCountryQuery() {
            return countryCode != null && !countryCode.trim().isEmpty();
        }
    }

    sealed interface Result {
        
        record Success(List<Currency> currencies, long count) implements Result {
            
            public Success {
                if (currencies == null) {
                    throw new IllegalArgumentException("Currencies cannot be null");
                }
                count = currencies.size();
            }
        }
        
        record ValidationError(Errors errors) implements Result {
        }
        
        record SystemError(Errors errors) implements Result {
        }
    }
}
