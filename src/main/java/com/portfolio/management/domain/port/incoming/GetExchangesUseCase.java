package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.model.Errors;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Use case for retrieving exchange information
 */
public interface GetExchangesUseCase {
    
    Uni<Result> execute(Query query);
    
    record Query(String searchTerm, String country, String currencyCode, RegionFilter regionFilter) {
        
        public Query() {
            this(null, null, null, null);
        }
        
        public Query(String searchTerm) {
            this(searchTerm, null, null, null);
        }
        
        public static Query byCountry(String country) {
            return new Query(null, country, null, null);
        }
        
        public static Query byCurrency(String currencyCode) {
            return new Query(null, null, currencyCode, null);
        }
        
        public static Query byRegion(RegionFilter regionFilter) {
            return new Query(null, null, null, regionFilter);
        }
        
        public boolean isSearchQuery() {
            return searchTerm != null && !searchTerm.trim().isEmpty();
        }
        
        public boolean isCountryQuery() {
            return country != null && !country.trim().isEmpty();
        }
        
        public boolean isCurrencyQuery() {
            return currencyCode != null && !currencyCode.trim().isEmpty();
        }
        
        public boolean isRegionQuery() {
            return regionFilter != null;
        }
    }
    
    enum RegionFilter {
        US, EUROPE, ASIA
    }

    sealed interface Result {
        
        record Success(List<Exchange> exchanges, long count) implements Result {
            
            public Success {
                if (exchanges == null) {
                    throw new IllegalArgumentException("Exchanges cannot be null");
                }
                count = exchanges.size();
            }
        }
        
        record ValidationError(Errors errors) implements Result {
        }
        
        record SystemError(Errors errors) implements Result {
        }
    }
}
