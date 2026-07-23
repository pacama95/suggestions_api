package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
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

    public static final int MAX_CANDIDATE_LIMIT = 300;
    public static final int MAX_ADVANCED_SEARCH_LIMIT = 100;

    /**
     * Single query: filters candidates by symbol/name LIKE, then ranks them by match
     * quality (exact symbol/name > prefix > contains) so the priority-strategy chain
     * downstream receives already-ordered candidates instead of two unordered batches
     * that need Java-side dedup.
     */
    private static final String FIND_CANDIDATES_QUERY = """
            from StockEntity
            where isActive = true
              and (lower(symbol) like ?1 or lower(name) like ?1)
            order by
              case
                when upper(symbol) = ?2 then 0
                when upper(name) = ?2 then 1
                when lower(symbol) like ?3 then 2
                when lower(name) like ?3 then 3
                else 4
              end,
              popularityScore desc,
              symbol asc
            """;

    public Uni<List<StockEntity>> findCandidateStocks(String query, int limit) {
        String trimmedQuery = query.trim();
        int maxLimit = Math.max(1, Math.min(limit, MAX_CANDIDATE_LIMIT));

        String likeParam = "%" + trimmedQuery.toLowerCase() + "%";
        String exactParam = trimmedQuery.toUpperCase();
        String prefixParam = trimmedQuery.toLowerCase() + "%";

        return find(FIND_CANDIDATES_QUERY, likeParam, exactParam, prefixParam)
                .page(0, maxLimit)
                .list();
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

    private Uni<List<StockEntity>> findByAdvancedSearch(Map<String, String> searchCriteria, int limit) {
        return Uni.createFrom().item(() -> validateAndBuildQuery(searchCriteria))
                .flatMap(queryData -> find(queryData.query(), queryData.parameters().toArray())
                        .page(0, Math.max(1, Math.min(limit, MAX_ADVANCED_SEARCH_LIMIT)))
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
                query.append(" AND lower(").append(entityField).append(") LIKE lower(?").append(paramIndex).append(")");
                parameters.add("%" + value.trim() + "%");
                paramIndex++;
            }
        }

        query.append(" ORDER BY popularityScore DESC, symbol ASC");

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

    /**
     * Refreshes planner statistics after a full reload (ingestion deletes and re-inserts
     * every row), since a stale stats snapshot would otherwise misjudge the new trigram
     * index selectivity.
     */
    public Uni<Void> analyzeTable() {
        return getSession()
                .chain(session -> session.createNativeQuery("ANALYZE stocks").executeUpdate())
                .replaceWithVoid();
    }
}
