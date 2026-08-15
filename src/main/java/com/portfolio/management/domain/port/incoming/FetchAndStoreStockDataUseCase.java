package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.StockFilter;
import io.smallrye.mutiny.Uni;

public interface FetchAndStoreStockDataUseCase {

    Uni<Result> fetchAndStoreStocks(StockFilter filter);

    /**
     * Appends ETF listings without clearing the existing catalog. Use this to ingest
     * ETFs into a populated database; {@link #fetchAndStoreStocks} already includes
     * ETFs on a full refresh.
     */
    Uni<Result> fetchAndStoreEtfs(StockFilter filter);

    sealed interface Result {
        record Success(boolean success, int recordsProcessed, String message) implements Result{}
        record Error(String message) implements Result{}
    }
}
