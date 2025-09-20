package com.portfolio.management.infrastructure.adapters.outgoing.client;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.outgoing.MarketDataPort;
import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
import com.portfolio.management.infrastructure.adapters.outgoing.client.mapper.StockMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

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

    public Uni<List<Stock>> fetchAllStocks() {
        Log.infof("Fetching available stocks from market data provider");
        return client.getAllStocks(apiKey)
                .map(stockMapper::toStocks)
                .invoke(stocks -> Log.infof("%d stocks fetched from market data provider", stocks.size()))
                .onFailure().invoke(throwable -> {
                    Log.errorf(throwable, "Failed to fetch stocks from TwelveData API");
                });
    }
}
