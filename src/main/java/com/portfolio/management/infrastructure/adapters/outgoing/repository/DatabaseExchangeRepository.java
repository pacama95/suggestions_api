package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.ExchangeEntity;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExchangeEntity using Reactive Panache with caching
 */
@ApplicationScoped
public class DatabaseExchangeRepository implements PanacheRepository<ExchangeEntity> {

    private static final List<String> US_COUNTRIES = List.of("United States");
    private static final List<String> EUROPEAN_COUNTRIES = List.of("Germany", "United Kingdom", "Italy", "Austria", "Europe");
    private static final List<String> ASIAN_COUNTRIES = List.of("Japan", "China", "Hong Kong", "South Korea", "Taiwan", "India", "Thailand");

    /**
     * Find all active exchanges - cached
     */
    @CacheResult(cacheName = "exchanges-all")
    @WithSession
    public Uni<List<ExchangeEntity>> findAllActive() {
        return find("isActive = true", Sort.ascending("name"))
                .list();
    }

    /**
     * Search exchanges by query string - matches code, name, or country
     */
    @CacheResult(cacheName = "exchanges-search")
    @WithSession
    public Uni<List<ExchangeEntity>> searchActive(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllActive();
        }

        String likeQuery = "%" + query.toLowerCase() + "%";
        return find("isActive = true AND (lower(code) LIKE ?1 OR lower(name) LIKE ?1 OR lower(country) LIKE ?1)",
                Sort.ascending("name"), likeQuery)
                .list();
    }

    /**
     * Find exchange by code - cached
     */
    @CacheResult(cacheName = "exchanges-by-code")
    @WithSession
    public Uni<Optional<ExchangeEntity>> findActiveByCode(String code) {
        return find("isActive = true AND upper(code) = upper(?1)", code)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /**
     * Find exchange by ID - cached
     */
    @CacheResult(cacheName = "exchanges-by-id")
    @WithSession
    public Uni<Optional<ExchangeEntity>> findActiveById(Long id) {
        return find("isActive = true AND id = ?1", id)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /**
     * Find exchanges by country - cached
     */
    @CacheResult(cacheName = "exchanges-by-country")
    @WithSession
    public Uni<List<ExchangeEntity>> findActiveByCountry(String country) {
        return find("isActive = true AND upper(country) = upper(?1)",
                Sort.ascending("name"), country)
                .list();
    }

    /**
     * Find exchanges by currency code - cached
     */
    @CacheResult(cacheName = "exchanges-by-currency")
    @WithSession
    public Uni<List<ExchangeEntity>> findActiveByCurrencyCode(String currencyCode) {
        return find("isActive = true AND upper(currencyCode) = upper(?1)",
                Sort.ascending("name"), currencyCode)
                .list();
    }

    /**
     * Find US exchanges - cached
     */
    @CacheResult(cacheName = "exchanges-us")
    @WithSession
    public Uni<List<ExchangeEntity>> findUSExchanges() {
        return find("isActive = true AND (country IN ?1 OR code IN ('NYSE', 'NASDAQ', 'OTC'))",
                Sort.ascending("name"), US_COUNTRIES)
                .list();
    }

    /**
     * Find European exchanges - cached
     */
    @CacheResult(cacheName = "exchanges-european")
    @WithSession
    public Uni<List<ExchangeEntity>> findEuropeanExchanges() {
        return find("isActive = true AND (country IN ?1 OR country LIKE '%Germany%' OR country LIKE '%Europe%')",
                Sort.ascending("name"), EUROPEAN_COUNTRIES)
                .list();
    }

    /**
     * Find Asian exchanges - cached
     */
    @CacheResult(cacheName = "exchanges-asian")
    @WithSession
    public Uni<List<ExchangeEntity>> findAsianExchanges() {
        return find("isActive = true AND (country IN ?1 OR country LIKE '%Asia%')",
                Sort.ascending("name"), ASIAN_COUNTRIES)
                .list();
    }

    /**
     * Count active exchanges - cached
     */
    @CacheResult(cacheName = "exchanges-count")
    @WithSession
    public Uni<Long> countActive() {
        return count("isActive = true");
    }
}
