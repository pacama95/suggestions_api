package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockTypeEntity;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StockTypeEntity using Reactive Panache with caching
 */
@ApplicationScoped
public class DatabaseStockTypeRepository implements PanacheRepository<StockTypeEntity> {
    
    /**
     * Find all active stock types - cached
     */
    @CacheResult(cacheName = "stock-types-all")
    public Uni<List<StockTypeEntity>> findAllActive() {
        return find("isActive = true", Sort.ascending("name"))
               .list();
    }
    
    /**
     * Search stock types by query string - matches code or name
     */
    @CacheResult(cacheName = "stock-types-search")
    public Uni<List<StockTypeEntity>> searchActive(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllActive();
        }
        
        String likeQuery = "%" + query.toLowerCase() + "%";
        return find("isActive = true AND (lower(code) LIKE ?1 OR lower(name) LIKE ?1)", 
                   Sort.ascending("name"), likeQuery)
               .list();
    }
    
    /**
     * Find stock type by code - cached
     */
    @CacheResult(cacheName = "stock-types-by-code")
    public Uni<Optional<StockTypeEntity>> findActiveByCode(String code) {
        return find("isActive = true AND upper(code) = upper(?1)", code)
               .firstResult()
               .map(Optional::ofNullable);
    }
    
    /**
     * Find stock type by ID - cached
     */
    @CacheResult(cacheName = "stock-types-by-id")
    public Uni<Optional<StockTypeEntity>> findActiveById(Long id) {
        return find("isActive = true AND id = ?1", id)
               .firstResult()
               .map(Optional::ofNullable);
    }
    
    /**
     * Count active stock types - cached
     */
    @CacheResult(cacheName = "stock-types-count")
    public Uni<Long> countActive() {
        return count("isActive = true");
    }
}
