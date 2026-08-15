package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import com.portfolio.management.domain.port.outgoing.MarketDataPort;
import com.portfolio.management.domain.port.outgoing.PopularityPort;
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
    private final PopularityPort popularityPort;
    private final Optional<List<String>> defaultCountries;
    private final Optional<List<String>> defaultExchanges;

    private static final int BATCH_SIZE = 1000;

    public FetchAndStoreStockDataService(MarketDataPort marketDataPort,
                                         StockPort stockPort,
                                         PopularityPort popularityPort,
                                         @ConfigProperty(name = "stocks.fetch.filter.countries")
                                         Optional<List<String>> defaultCountries,
                                         @ConfigProperty(name = "stocks.fetch.filter.exchanges")
                                         Optional<List<String>> defaultExchanges) {
        this.marketDataPort = marketDataPort;
        this.stockPort = stockPort;
        this.popularityPort = popularityPort;
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

    @Override
    public Uni<Result> fetchAndStoreEtfs(StockFilter filter) {
        StockFilter effectiveFilter = mergeWithDefaults(filter);
        Log.infof("Fetching and appending ETFs with filter: %s", effectiveFilter);

        return marketDataPort.fetchEtfs(effectiveFilter)
                .onItem().transformToUni(etfs -> {
                    if (etfs.isEmpty()) {
                        return Uni.createFrom().item(() ->
                                new Result.Success(false, 0, "No ETFs fetched from market data provider"));
                    }
                    return appendListings(etfs, "ETFs");
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(throwable, "Failed to fetch and store ETFs");
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
        return new StockFilter(countries, exchanges, requestFilter.symbols() != null ? requestFilter.symbols() : List.of());
    }

    private boolean hasValues(List<String> values) {
        return values != null && !values.isEmpty();
    }

    private Uni<Result> processAndStoreStocks(List<Stock> fetchedStocks) {
        return clearExistingStocks()
                .invoke(stocksCleared -> Log.infof("%d stocks cleared", stocksCleared))
                .flatMap(ignored -> storeStocksInBatches(fetchedStocks))
                .flatMap(ignored -> popularityPort.recomputeStockScores())
                .flatMap(ignored -> stockPort.analyzeTable())
                .replaceWith(() -> new Result.Success(true, fetchedStocks.size(), "Successfully fetched and stored stocks"));
    }

    private Uni<Result> appendListings(List<Stock> listings, String label) {
        Log.infof("Appending %d %s without clearing the existing catalog", listings.size(), label);
        return storeStocksInBatches(listings)
                .flatMap(ignored -> stockPort.analyzeTable())
                .replaceWith(() -> new Result.Success(
                        true, listings.size(), "Successfully fetched and stored " + label));
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
