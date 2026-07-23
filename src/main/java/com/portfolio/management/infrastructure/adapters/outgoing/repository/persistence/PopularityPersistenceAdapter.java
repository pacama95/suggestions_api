package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.port.outgoing.PopularityPort;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseStockPopularityRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PopularityPersistenceAdapter implements PopularityPort {

    private final DatabaseStockPopularityRepository databaseStockPopularityRepository;

    public PopularityPersistenceAdapter(DatabaseStockPopularityRepository databaseStockPopularityRepository) {
        this.databaseStockPopularityRepository = databaseStockPopularityRepository;
    }

    @Override
    @WithTransaction
    public Uni<Void> recomputeStockScores() {
        return databaseStockPopularityRepository.recomputeAllStockScores();
    }

    @Override
    @WithTransaction
    public Uni<Void> incrementUsage(String symbol, String exchange, String currency) {
        return databaseStockPopularityRepository.upsertIncrement(symbol)
                .chain(() -> databaseStockPopularityRepository.recomputeStockScore(symbol))
                .chain(() -> bumpExchangeIfPresent(exchange))
                .chain(() -> bumpCurrencyIfPresent(currency));
    }

    private Uni<Void> bumpExchangeIfPresent(String exchange) {
        if (exchange == null || exchange.isBlank()) {
            return Uni.createFrom().voidItem();
        }
        return databaseStockPopularityRepository.bumpExchangePopularity(exchange);
    }

    private Uni<Void> bumpCurrencyIfPresent(String currency) {
        if (currency == null || currency.isBlank()) {
            return Uni.createFrom().voidItem();
        }
        return databaseStockPopularityRepository.bumpCurrencyPopularity(currency);
    }
}
