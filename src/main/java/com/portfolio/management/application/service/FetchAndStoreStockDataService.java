package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import com.portfolio.management.domain.port.outgoing.MarketDataPort;
import com.portfolio.management.domain.port.outgoing.StockPort;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FetchAndStoreStockDataService implements FetchAndStoreStockDataUseCase {

    private final MarketDataPort marketDataPort;
    private final StockPort stockPort;
    private final Optional<List<String>> defaultCountries;
    private final Optional<List<String>> defaultExchanges;

    private static final int BATCH_SIZE = 1000;

    public FetchAndStoreStockDataService(MarketDataPort marketDataPort,
                                         StockPort stockPort,
                                         @ConfigProperty(name = "stocks.fetch.filter.countries")
                                         Optional<List<String>> defaultCountries,
                                         @ConfigProperty(name = "stocks.fetch.filter.exchanges")
                                         Optional<List<String>> defaultExchanges) {
        this.marketDataPort = marketDataPort;
        this.stockPort = stockPort;
        this.defaultCountries = defaultCountries;
        this.defaultExchanges = defaultExchanges;
    }

    @Override
    public Uni<Result> fetchAndStoreStocks(StockFilter filter) {
        StockFilter effectiveFilter = mergeWithDefaults(filter);
        Log.infof("Fetching and storing stocks with filter: %s", effectiveFilter);

        return marketDataPort.fetchStocks(effectiveFilter)
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

    StockFilter mergeWithDefaults(StockFilter requestFilter) {
        List<String> countries = hasValues(requestFilter.countries())
                ? requestFilter.countries()
                : defaultCountries.orElse(List.of());
        List<String> exchanges = hasValues(requestFilter.exchanges())
                ? requestFilter.exchanges()
                : defaultExchanges.orElse(List.of());
        return new StockFilter(countries, exchanges);
    }

    private boolean hasValues(List<String> values) {
        return values != null && !values.isEmpty();
    }

    private Uni<Result> processAndStoreStocks(List<Stock> fetchedStocks) {
        return clearExistingStocks()
                .invoke(stocksCleared -> Log.infof("%d stocks cleared", stocksCleared))
                .flatMap(ignored -> storeStocksInBatches(fetchedStocks))
                .flatMap(ignored -> stockPort.analyzeTable())
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
