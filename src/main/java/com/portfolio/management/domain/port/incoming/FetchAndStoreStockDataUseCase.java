package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.StockFilter;
import io.smallrye.mutiny.Uni;

public interface FetchAndStoreStockDataUseCase {

    Uni<Result> fetchAndStoreStocks(StockFilter filter);

    sealed interface Result {
        record Success(boolean success, int recordsProcessed, String message) implements Result{}
        record Error(String message) implements Result{}
    }
}
