package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.domain.model.Errors;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Use case for retrieving stock type information
 */
public interface GetStockTypesUseCase {
    
    Uni<Result> execute(Query query);
    
    record Query(String searchTerm) {
        
        public Query() {
            this(null);
        }
        
        public boolean isSearchQuery() {
            return searchTerm != null && !searchTerm.trim().isEmpty();
        }
    }

    sealed interface Result {
        
        record Success(List<StockType> stockTypes, long count) implements Result {
            
            public Success {
                if (stockTypes == null) {
                    throw new IllegalArgumentException("Stock types cannot be null");
                }
                count = stockTypes.size();
            }
        }
        
        record ValidationError(Errors errors) implements Result {
        }
        
        record SystemError(Errors errors) implements Result {
        }
    }
}
