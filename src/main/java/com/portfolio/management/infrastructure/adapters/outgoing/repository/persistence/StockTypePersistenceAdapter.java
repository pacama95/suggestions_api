package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.domain.port.outgoing.StockTypeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseStockTypeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for StockType repository following hexagonal architecture
 */
@ApplicationScoped
@Named("database-stocktype-repository")
public class StockTypePersistenceAdapter implements StockTypeRepository {

    private final DatabaseStockTypeRepository stockTypeRepository;
    private final CategoryMapper categoryMapper;

    public StockTypePersistenceAdapter(
            DatabaseStockTypeRepository stockTypeRepository,
            CategoryMapper categoryMapper) {
        this.stockTypeRepository = stockTypeRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @WithSession
    public Uni<List<StockType>> findAll() {
        return stockTypeRepository.findAllActive()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toStockType)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<List<StockType>> search(String query) {
        return stockTypeRepository.searchActive(query)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toStockType)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<Optional<StockType>> findByCode(String code) {
        return stockTypeRepository.findActiveByCode(code)
                .map(optEntity -> optEntity.map(categoryMapper::toStockType));
    }

    @Override
    @WithSession
    public Uni<Optional<StockType>> findById(Long id) {
        return stockTypeRepository.findActiveById(id)
                .map(optEntity -> optEntity.map(categoryMapper::toStockType));
    }

    @Override
    @WithSession
    public Uni<Long> count() {
        return stockTypeRepository.countActive();
    }
}
