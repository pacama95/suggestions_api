package com.portfolio.management.infrastructure.adapters.outgoing.client;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.domain.port.outgoing.MarketDataPort;
import com.portfolio.management.infrastructure.adapters.outgoing.client.mapper.StockMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MarketDataAdapter implements MarketDataPort {

    private final TwelveDataClient client;
    private final StockMapper stockMapper;
    private final String apiKey;

    public MarketDataAdapter(@RestClient TwelveDataClient client,
                             StockMapper stockMapper,
                             @ConfigProperty(name = "twelve.data.api.key") String apiKey) {
        this.client = client;
        this.stockMapper = stockMapper;
        this.apiKey = apiKey;
    }

    @Override
    public Uni<List<Stock>> fetchStocks(StockFilter filter) {
        Log.infof("Fetching available stocks and ETFs from market data provider with filter: %s", filter);
        return fetchListings(filter, true)
                .invoke(stocks -> Log.infof(
                        "%d stocks and ETFs fetched from market data provider after deduplication", stocks.size()))
                .onFailure().invoke(throwable ->
                        Log.errorf(throwable, "Failed to fetch stocks from TwelveData API"));
    }

    @Override
    public Uni<List<Stock>> fetchEtfs(StockFilter filter) {
        Log.infof("Fetching available ETFs from market data provider with filter: %s", filter);
        return fetchListings(filter, false)
                .invoke(etfs -> Log.infof(
                        "%d ETFs fetched from market data provider after deduplication", etfs.size()))
                .onFailure().invoke(throwable ->
                        Log.errorf(throwable, "Failed to fetch ETFs from TwelveData API"));
    }

    private Uni<List<Stock>> fetchListings(StockFilter filter, boolean includeStocks) {
        List<String> countries = normalizeFilterList(filter.countries());
        List<String> exchanges = normalizeFilterList(filter.exchanges());

        return Multi.createFrom().iterable(buildCombinations(countries, exchanges))
                .onItem().transformToUniAndConcatenate(combination -> fetchCombination(combination, includeStocks))
                .collect().asList()
                .map(this::deduplicateStocks);
    }

    private Uni<List<Stock>> fetchCombination(FilterCombination combination, boolean includeStocks) {
        Uni<List<Stock>> etfs = client.getEtfs(apiKey, combination.exchange(), combination.country())
                .map(stockMapper::toEtfs)
                .invoke(fetched -> Log.infof(
                        "Fetched %d ETFs for exchange=%s country=%s",
                        fetched.size(), combination.exchange(), combination.country()));

        if (!includeStocks) {
            return etfs;
        }

        return client.getStocks(apiKey, combination.exchange(), combination.country())
                .map(stockMapper::toStocks)
                .invoke(stocks -> Log.infof(
                        "Fetched %d stocks for exchange=%s country=%s",
                        stocks.size(), combination.exchange(), combination.country()))
                .flatMap(stocks -> etfs.map(etfList -> concat(stocks, etfList)));
    }

    private static List<Stock> concat(List<Stock> first, List<Stock> second) {
        List<Stock> combined = new ArrayList<>(first.size() + second.size());
        combined.addAll(first);
        combined.addAll(second);
        return combined;
    }

    private List<String> normalizeFilterList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.singletonList(null);
        }
        return values;
    }

    private List<FilterCombination> buildCombinations(List<String> countries, List<String> exchanges) {
        List<FilterCombination> combinations = new ArrayList<>();
        for (String country : countries) {
            for (String exchange : exchanges) {
                combinations.add(new FilterCombination(exchange, country));
            }
        }
        return combinations;
    }

    private List<Stock> deduplicateStocks(List<List<Stock>> stockLists) {
        Map<String, Stock> uniqueStocks = new LinkedHashMap<>();
        for (List<Stock> stocks : stockLists) {
            for (Stock stock : stocks) {
                String key = stock.symbol() + "|" + (stock.micCode() != null ? stock.micCode() : "");
                uniqueStocks.putIfAbsent(key, stock);
            }
        }
        return new ArrayList<>(uniqueStocks.values());
    }

    private record FilterCombination(String exchange, String country) {}
}
