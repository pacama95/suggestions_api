package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.model.StockProcessingResult;
import com.portfolio.management.domain.model.StocksBatchProcessingResult;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseStockRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.StockMapper;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StockPersistenceAdapter implements StockPort {

    private final StockMapper stockMapper;
    private final DatabaseStockRepository databaseStockRepository;

    public StockPersistenceAdapter(StockMapper stockMapper, DatabaseStockRepository databaseStockRepository) {
        this.stockMapper = stockMapper;
        this.databaseStockRepository = databaseStockRepository;
    }

    @Override
    @WithTransaction
    public Uni<Stock> save(Stock stock) {
        return Uni.createFrom().item(() -> stockMapper.toStockEntity(stock))
                .flatMap(databaseStockRepository::persistAndFlush)
                .map(stockMapper::toStock);
    }

    @Override
    @WithTransaction
    public Uni<StocksBatchProcessingResult> saveBatch(List<Stock> stocks) {
        return Uni.createFrom().item(() -> stocks.stream()
                        .map(stockMapper::toStockEntity)
                        .toList())
                .flatMap(databaseStockRepository::persistBatch)
                .onItem().transform(savedEntities -> {
                    long successCount = savedEntities.size();
                    long errorCount = 0;

                    Log.infof("Stocks batch save process complete: %d success, %d errors", successCount, errorCount);

                    return new StocksBatchProcessingResult(successCount, errorCount);
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(throwable, "Error during batch save, falling back to individual saves");
                    // Fallback to individual saves if batch fails
                    return null;
                })
                .onItem().ifNull().switchTo(() -> saveBatchIndividually(stocks));
    }

    /**
     * Fallback method for individual saves when batch operation fails
     */
    private Uni<StocksBatchProcessingResult> saveBatchIndividually(List<Stock> stocks) {
        return Multi.createFrom().iterable(stocks)
                .onItem().transformToUniAndConcatenate(stock -> save(stock)
                        .onItem().transform(savedStock -> new StockProcessingResult(savedStock, true, "OK"))
                        .onFailure().recoverWithItem(throwable -> new StockProcessingResult(stock, false, throwable.getMessage())))
                .collect().asList()
                .chain(results -> {
                    long successCount = results.stream().filter(StockProcessingResult::success).count();
                    long errorCount = results.stream().filter(s -> !s.success()).count();

                    Log.infof("Stocks individual save process complete: %d success, %d errors", successCount, errorCount);

                    return Uni.createFrom().item(() -> new StocksBatchProcessingResult(successCount, errorCount));
                });
    }

    @Override
    @WithSession
    public Uni<List<Stock>> findCandidateStocks(String query, int limit) {
        return databaseStockRepository.findCandidateStocks(query, limit)
                .map(stockMapper::toStocks);
    }

    @Override
    @WithSession
    public Uni<List<Stock>> findByAdvancedSearch(String symbol, String companyName, String exchange, String country, String currency, int limit) {
        return databaseStockRepository.findByAdvancedSearch(symbol, companyName, exchange, country, currency, limit)
                .map(stockMapper::toStocks);
    }

    @Override
    @WithTransaction
    public Uni<Long> deleteAll() {
        return databaseStockRepository.clearAll();
    }
}
