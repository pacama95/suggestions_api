package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.domain.model.Errors;
import io.smallrye.mutiny.Uni;

/**
 * Use case for retrieving a single stock type
 */
public interface GetStockTypeUseCase {
    
    Uni<Result> execute(Query query);
    
    record Query(Long id, String code) {
        
        public Query {
            if (id == null && (code == null || code.trim().isEmpty())) {
                throw new IllegalArgumentException("Either id or code must be provided");
            }
        }
        
        public static Query byId(Long id) {
            return new Query(id, null);
        }
        
        public static Query byCode(String code) {
            return new Query(null, code);
        }
        
        public boolean isIdQuery() {
            return id != null;
        }
        
        public boolean isCodeQuery() {
            return code != null && !code.trim().isEmpty();
        }
    }

    sealed interface Result {
        
        record Success(StockType stockType) implements Result {
            
            public Success {
                if (stockType == null) {
                    throw new IllegalArgumentException("Stock type cannot be null");
                }
            }
        }
        
        record NotFound() implements Result {
        }
        
        record ValidationError(Errors errors) implements Result {
        }
        
        record SystemError(Errors errors) implements Result {
        }
    }
}
