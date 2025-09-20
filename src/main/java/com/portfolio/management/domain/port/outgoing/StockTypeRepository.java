package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.StockType;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for stock type operations
 */
public interface StockTypeRepository {
    
    /**
     * Find all active stock types
     * @return List of all active stock types
     */
    Uni<List<StockType>> findAll();
    
    /**
     * Search stock types by query string (matches code or name)
     * @param query Search query
     * @return List of matching stock types
     */
    Uni<List<StockType>> search(String query);
    
    /**
     * Find stock type by code
     * @param code Stock type code
     * @return Optional stock type if found
     */
    Uni<Optional<StockType>> findByCode(String code);
    
    /**
     * Find stock type by ID
     * @param id Stock type ID
     * @return Optional stock type if found
     */
    Uni<Optional<StockType>> findById(Long id);
    
    /**
     * Count total active stock types
     * @return Total count of active stock types
     */
    Uni<Long> count();
}
