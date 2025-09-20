package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * Repository for StockEntity using Reactive Panache
 */
@ApplicationScoped
public class DatabaseStockRepository implements PanacheRepository<StockEntity> {

    /**
     * Find active stocks matching the query string - truly reactive
     */
    public Uni<List<StockEntity>> findActiveByQuery(String query, int limit) {
        String likeQuery = "%" + query.toLowerCase() + "%";
        return find("isActive = true AND (lower(symbol) LIKE ?1 OR lower(name) LIKE ?1)",
                Sort.ascending("symbol"), likeQuery)
                .page(0, Math.max(1, Math.min(limit, 100)))
                .list();
    }

    /**
     * Find active stock by symbol - truly reactive
     */
    public Uni<Optional<StockEntity>> findActiveBySymbol(String symbol) {
        return find("isActive = true AND upper(symbol) = upper(?1)", symbol)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /**
     * Count active stocks - truly reactive
     */
    public Uni<Long> countActive() {
        return count("isActive = true");
    }

    /**
     * Advanced search with multiple optional criteria - truly reactive
     * At least one search parameter must be provided (non-null and non-empty)
     */
    public Uni<List<StockEntity>> findByAdvancedSearch(
            String symbol, String companyName, String exchange, String country, String currency, int limit) {

        Map<String, String> searchCriteria = new HashMap<>();
        if (symbol != null) searchCriteria.put("symbol", symbol);
        if (companyName != null) searchCriteria.put("name", companyName);
        if (exchange != null) searchCriteria.put("exchange", exchange);
        if (country != null) searchCriteria.put("country", country);
        if (currency != null) searchCriteria.put("currency", currency);

        return findByAdvancedSearch(searchCriteria, limit);
    }

    /**
     * Advanced search with flexible criteria map - truly reactive
     * At least one search parameter must be provided (non-null and non-empty)
     */
    private Uni<List<StockEntity>> findByAdvancedSearch(Map<String, String> searchCriteria, int limit) {
        return Uni.createFrom().item(() -> validateAndBuildQuery(searchCriteria))
                .flatMap(queryData -> find(queryData.query(), queryData.parameters().toArray())
                        .page(0, Math.max(1, Math.min(limit, 100)))
                        .list());
    }

    /**
     * Validates search criteria and builds the query with parameters
     */
    private QueryData validateAndBuildQuery(Map<String, String> searchCriteria) {
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            throw new IllegalArgumentException("Search criteria cannot be null or empty");
        }

        boolean hasValidCriteria = searchCriteria.values().stream()
                .anyMatch(value -> value != null && !value.trim().isEmpty());

        if (!hasValidCriteria) {
            throw new IllegalArgumentException("At least one search parameter must be non-null and non-empty");
        }

        StringBuilder query = new StringBuilder("isActive = true");
        List<String> parameters = new ArrayList<>();
        int paramIndex = 1;

        // Map field names to entity properties
        Map<String, String> fieldMapping = Map.of(
                "symbol", "symbol",
                "name", "name",
                "exchange", "exchange",
                "country", "country",
                "currency", "currency"
        );

        for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();

            if (value != null && !value.trim().isEmpty() && fieldMapping.containsKey(field)) {
                String entityField = fieldMapping.get(field);
                query.append(" AND upper(").append(entityField).append(") LIKE upper(?").append(paramIndex).append(")");
                parameters.add("%" + value.trim() + "%");
                paramIndex++;
            }
        }

        return new QueryData(query.toString(), parameters);
    }

    /**
     * Simple search by symbol only - truly reactive
     */
    public Uni<List<StockEntity>> findBySymbol(String symbol, int limit) {
        return findByAdvancedSearch(Map.of("symbol", symbol), limit);
    }

    /**
     * Simple search by company name only - truly reactive
     */
    public Uni<List<StockEntity>> findByCompanyName(String companyName, int limit) {
        return findByAdvancedSearch(Map.of("name", companyName), limit);
    }

    /**
     * Search with multiple criteria using builder pattern - truly reactive
     */
    public static class SearchCriteriaBuilder {
        private final Map<String, String> criteria = new HashMap<>();

        public SearchCriteriaBuilder symbol(String symbol) {
            if (symbol != null) criteria.put("symbol", symbol);
            return this;
        }

        public SearchCriteriaBuilder companyName(String companyName) {
            if (companyName != null) criteria.put("name", companyName);
            return this;
        }

        public SearchCriteriaBuilder exchange(String exchange) {
            if (exchange != null) criteria.put("exchange", exchange);
            return this;
        }

        public SearchCriteriaBuilder country(String country) {
            if (country != null) criteria.put("country", country);
            return this;
        }

        public SearchCriteriaBuilder currency(String currency) {
            if (currency != null) criteria.put("currency", currency);
            return this;
        }

        public Map<String, String> build() {
            return new HashMap<>(criteria);
        }
    }

    /**
     * Create a new search criteria builder
     */
    public static SearchCriteriaBuilder searchCriteria() {
        return new SearchCriteriaBuilder();
    }

    /**
     * Record to hold query and parameters data
     */
    private record QueryData(String query, List<String> parameters) {
    }

    public Uni<Long> clearAll() {
        return deleteAll();
    }

    public Uni<List<StockEntity>> persistBatch(List<StockEntity> stockEntities) {
        return persist(stockEntities).replaceWith(stockEntities);
    }
}
