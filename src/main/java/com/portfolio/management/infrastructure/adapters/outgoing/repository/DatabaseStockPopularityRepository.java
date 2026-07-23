package com.portfolio.management.infrastructure.adapters.outgoing.repository;

import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockPopularityEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository backing the stock_popularity survivor table and the popularity_score
 * columns it feeds on stocks/exchanges/currencies. Uses native SQL rather than
 * Panache finders since these are bulk recompute/upsert operations, not entity CRUD.
 */
@ApplicationScoped
public class DatabaseStockPopularityRepository implements PanacheRepositoryBase<StockPopularityEntity, String> {

    /**
     * score = static_score + 0.25 * exchange tier + 10 * ln(1 + tx_count).
     * Recomputed via a subquery join (rather than a multi-table UPDATE...FROM)
     * because Postgres can't express two independent LEFT JOINs against the
     * target table directly in an UPDATE's FROM clause.
     */
    private static final String RECOMPUTE_ALL_SCORES_UPDATE = """
            UPDATE stocks s
            SET popularity_score = COALESCE(joined.static_score, 0)
                                  + 0.25 * COALESCE(joined.exchange_score, 0)
                                  + 10 * LN(1 + COALESCE(joined.tx_count, 0))
            FROM (
                SELECT st.id AS stock_id,
                       sp.static_score,
                       sp.tx_count,
                       e.popularity_score AS exchange_score
                FROM stocks st
                LEFT JOIN stock_popularity sp ON sp.symbol = st.symbol
                LEFT JOIN exchanges e ON e.code = st.exchange
            ) AS joined
            WHERE joined.stock_id = s.id
            """;

    private static final String RECOMPUTE_ONE_SCORE_UPDATE = """
            UPDATE stocks s
            SET popularity_score = COALESCE(joined.static_score, 0)
                                  + 0.25 * COALESCE(joined.exchange_score, 0)
                                  + 10 * LN(1 + COALESCE(joined.tx_count, 0))
            FROM (
                SELECT st.id AS stock_id,
                       sp.static_score,
                       sp.tx_count,
                       e.popularity_score AS exchange_score
                FROM stocks st
                LEFT JOIN stock_popularity sp ON sp.symbol = st.symbol
                LEFT JOIN exchanges e ON e.code = st.exchange
                WHERE st.symbol = :symbol
            ) AS joined
            WHERE joined.stock_id = s.id
            """;

    private static final String UPSERT_INCREMENT = """
            INSERT INTO stock_popularity (symbol, static_score, tx_count, updated_at)
            VALUES (:symbol, 0, 1, now())
            ON CONFLICT (symbol) DO UPDATE
                SET tx_count = stock_popularity.tx_count + 1,
                    updated_at = now()
            """;

    private static final String BUMP_EXCHANGE_POPULARITY =
            "UPDATE exchanges SET popularity_score = popularity_score + 1 WHERE code = :exchange OR name = :exchange";

    private static final String BUMP_CURRENCY_POPULARITY =
            "UPDATE currencies SET popularity_score = popularity_score + 1 WHERE code = :currency";

    public Uni<Void> recomputeAllStockScores() {
        return getSession()
                .chain(session -> session.createNativeQuery(RECOMPUTE_ALL_SCORES_UPDATE).executeUpdate())
                .replaceWithVoid();
    }

    public Uni<Void> recomputeStockScore(String symbol) {
        return getSession()
                .chain(session -> session.createNativeQuery(RECOMPUTE_ONE_SCORE_UPDATE)
                        .setParameter("symbol", symbol)
                        .executeUpdate())
                .replaceWithVoid();
    }

    public Uni<Void> upsertIncrement(String symbol) {
        return getSession()
                .chain(session -> session.createNativeQuery(UPSERT_INCREMENT)
                        .setParameter("symbol", symbol)
                        .executeUpdate())
                .replaceWithVoid();
    }

    public Uni<Void> bumpExchangePopularity(String exchange) {
        return getSession()
                .chain(session -> session.createNativeQuery(BUMP_EXCHANGE_POPULARITY)
                        .setParameter("exchange", exchange)
                        .executeUpdate())
                .replaceWithVoid();
    }

    public Uni<Void> bumpCurrencyPopularity(String currency) {
        return getSession()
                .chain(session -> session.createNativeQuery(BUMP_CURRENCY_POPULARITY)
                        .setParameter("currency", currency)
                        .executeUpdate())
                .replaceWithVoid();
    }
}
