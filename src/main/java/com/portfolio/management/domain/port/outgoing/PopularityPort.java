package com.portfolio.management.domain.port.outgoing;

import io.smallrye.mutiny.Uni;

/**
 * Outgoing port for the popularity signal that blends a curated static baseline
 * (exchange/currency tiers, household-name tickers) with observed transaction
 * usage to rank suggestions.
 */
public interface PopularityPort {

    /**
     * Recomputes stocks.popularity_score for every stock from the current
     * stock_popularity/exchange tiers. Must run after ingestion reloads the
     * stocks table (which wipes and reinserts every row) so popularity survives
     * the reload.
     */
    Uni<Void> recomputeStockScores();

    /**
     * Records one observed transaction for a symbol: increments its usage count,
     * recomputes its stock score, and nudges its exchange/currency popularity.
     */
    Uni<Void> incrementUsage(String symbol, String exchange, String currency);
}
