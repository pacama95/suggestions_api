package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for StockEntity using Reactive Panache
 */
@ApplicationScoped
public class DatabaseStockRepository implements PanacheRepository<StockEntity> {

    public Uni<List<StockEntity>> findCandidateStocks(String query, int limit) {
        String trimmedQuery = query.trim();
        int maxLimit = Math.max(1, Math.min(limit, 300));

        return findExactMatches(trimmedQuery, maxLimit)
                .chain(exactMatches -> {
                    int remaining = maxLimit - exactMatches.size();
                    if (remaining <= 0) {
                        return Uni.createFrom().item(exactMatches);
                    }

                    return findPartialMatches(trimmedQuery, remaining, exactMatches)
                            .map(partialMatches -> combineResults(exactMatches, partialMatches));
                });
    }

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

    private Uni<List<StockEntity>> findExactMatches(String query, int limit) {
        String queryUpper = query.toUpperCase();
        return find("isActive = true AND (upper(symbol) = ?1 OR upper(name) = ?1)",
                Sort.ascending("symbol"), queryUpper)
                .page(0, limit)
                .list();
    }

    private Uni<List<StockEntity>> findPartialMatches(String query, int limit, List<StockEntity> existingResults) {
        String queryLower = query.toLowerCase();
        String likeQuery = "%" + queryLower + "%";

        return find("isActive = true AND (lower(symbol) LIKE ?1 OR lower(name) LIKE ?1)",
                Sort.ascending("symbol"), likeQuery)
                .page(0, limit + existingResults.size()) // Fetch more to account for duplicates
                .list()
                .map(results -> filterDuplicates(results, existingResults, limit));
    }

    private List<StockEntity> combineResults(List<StockEntity> exactMatches, List<StockEntity> partialMatches) {
        var combined = new java.util.ArrayList<>(exactMatches);
        combined.addAll(partialMatches);
        return combined;
    }

    private List<StockEntity> filterDuplicates(List<StockEntity> candidates, List<StockEntity> existing, int limit) {
        return candidates.stream()
                .filter(candidate -> existing.stream().noneMatch(ex -> ex.id.equals(candidate.id)))
                .limit(limit)
                .toList();
    }

    private Uni<List<StockEntity>> findByAdvancedSearch(Map<String, String> searchCriteria, int limit) {
        return Uni.createFrom().item(() -> validateAndBuildQuery(searchCriteria))
                .flatMap(queryData -> find(queryData.query(), queryData.parameters().toArray())
                        .page(0, Math.max(1, Math.min(limit, 100)))
                        .list());
    }

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

    private record QueryData(String query, List<String> parameters) {
    }

    public Uni<Long> clearAll() {
        return deleteAll();
    }

    public Uni<List<StockEntity>> persistBatch(List<StockEntity> stockEntities) {
        return persist(stockEntities).replaceWith(stockEntities);
    }
}
