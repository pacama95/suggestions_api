package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StocksBatchProcessingResult;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface StockPort {

    Uni<Stock> save(Stock stock);

    Uni<StocksBatchProcessingResult> saveBatch(List<Stock> stocks);

    Uni<List<Stock>> findCandidateStocks(String query, int limit);

    Uni<List<Stock>> findByAdvancedSearch(String symbol, String companyName, String exchange, String region, String currency, String isin, int limit);

    Uni<Long> deleteAll();

    /**
     * Refreshes DB planner statistics for the stocks table. Best-effort: failures
     * are logged and swallowed by the adapter, never surfaced to callers.
     */
    Uni<Void> analyzeTable();
}
