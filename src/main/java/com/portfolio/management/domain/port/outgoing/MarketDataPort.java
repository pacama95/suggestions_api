package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.Stock;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface MarketDataPort {

    Uni<List<Stock>> fetchAllStocks();
}
