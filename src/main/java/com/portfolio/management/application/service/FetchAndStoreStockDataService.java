package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.infrastructure.adapters.outgoing.client.MarketDataAdapter;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FetchAndStoreStockDataService implements FetchAndStoreStockDataUseCase {

    private final MarketDataAdapter marketDataAdapter;
    private final StockPort stockPort;

    private static final int BATCH_SIZE = 1000;

    public FetchAndStoreStockDataService(MarketDataAdapter twelveDataService, StockPort stockPort) {
        this.marketDataAdapter = twelveDataService;
        this.stockPort = stockPort;
    }

    public Uni<Result> fetchAndStoreStocks() {
        return marketDataAdapter.fetchAllStocks()
                .onItem().transformToUni(stocks -> {
                    if (stocks.isEmpty()) {
                        return Uni.createFrom().item(() ->
                                new Result.Success(false, 0, "No stocks fetched from market data provider"));
                    } else {
                        return this.processAndStoreStocks(stocks);
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(throwable, "Failed to fetch and store stocks");
                    return new Result.Error("Failed: " + throwable.getMessage());
                });
    }

    private Uni<Result> processAndStoreStocks(List<Stock> fetchedStocks) {
        return clearExistingStocks()
                .invoke(stocksCleared -> Log.infof("%d sock cleared", stocksCleared))
                .flatMap(ignored -> storeStocksInBatches(fetchedStocks))
                .replaceWith(() -> new Result.Success(true, fetchedStocks.size(), "Successfully fetched and stored stocks"));
    }

    private Uni<Long> clearExistingStocks() {
        Log.info("Clearing existing stocks from database...");
        return stockPort.deleteAll();
    }

    private Uni<Void> storeStocksInBatches(List<Stock> stocks) {
        return Multi.createFrom().iterable(stocks)
                .group().intoLists().of(BATCH_SIZE)
                .invoke(stocksBatch -> Log.infof("Processing stocks batch..."))
                .onItem().transformToUniAndConcatenate(stockPort::saveBatch)
                .collect().asList()
                .replaceWithVoid();
    }
}
