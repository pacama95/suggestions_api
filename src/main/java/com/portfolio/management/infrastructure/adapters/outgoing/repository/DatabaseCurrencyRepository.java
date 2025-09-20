package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.CurrencyEntity;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CurrencyEntity using Reactive Panache with caching
 */
@ApplicationScoped
public class DatabaseCurrencyRepository implements PanacheRepository<CurrencyEntity> {
    
    private static final List<String> MAJOR_CURRENCIES = List.of("USD", "EUR", "JPY", "GBP", "CHF", "CAD", "AUD");
    
    /**
     * Find all active currencies - cached
     */
    @CacheResult(cacheName = "currencies-all")
    public Uni<List<CurrencyEntity>> findAllActive() {
        return find("isActive = true", Sort.ascending("code"))
               .list();
    }
    
    /**
     * Search currencies by query string - matches code, name, or country code
     */
    @CacheResult(cacheName = "currencies-search")
    public Uni<List<CurrencyEntity>> searchActive(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllActive();
        }
        
        String likeQuery = "%" + query.toLowerCase() + "%";
        return find("isActive = true AND (lower(code) LIKE ?1 OR lower(name) LIKE ?1 OR lower(countryCode) LIKE ?1)", 
                   Sort.ascending("code"), likeQuery)
               .list();
    }
    
    /**
     * Find currency by code - cached
     */
    @CacheResult(cacheName = "currencies-by-code")
    public Uni<Optional<CurrencyEntity>> findActiveByCode(String code) {
        return find("isActive = true AND upper(code) = upper(?1)", code)
               .firstResult()
               .map(Optional::ofNullable);
    }
    
    /**
     * Find currency by ID - cached
     */
    @CacheResult(cacheName = "currencies-by-id")
    public Uni<Optional<CurrencyEntity>> findActiveById(Long id) {
        return find("isActive = true AND id = ?1", id)
               .firstResult()
               .map(Optional::ofNullable);
    }
    
    /**
     * Find currencies by country code - cached
     */
    @CacheResult(cacheName = "currencies-by-country")
    public Uni<List<CurrencyEntity>> findActiveByCountryCode(String countryCode) {
        return find("isActive = true AND upper(countryCode) = upper(?1)", 
                   Sort.ascending("code"), countryCode)
               .list();
    }
    
    /**
     * Find major currencies - cached
     */
    @CacheResult(cacheName = "currencies-major")
    public Uni<List<CurrencyEntity>> findMajorCurrencies() {
        return find("isActive = true AND code IN ?1", Sort.ascending("code"), MAJOR_CURRENCIES)
               .list();
    }
    
    /**
     * Count active currencies - cached
     */
    @CacheResult(cacheName = "currencies-count")
    public Uni<Long> countActive() {
        return count("isActive = true");
    }
}
