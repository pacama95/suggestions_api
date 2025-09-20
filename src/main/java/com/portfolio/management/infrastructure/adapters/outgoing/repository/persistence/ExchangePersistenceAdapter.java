package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.port.outgoing.ExchangeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseExchangeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for Exchange repository following hexagonal architecture
 */
@ApplicationScoped
@Named("database-exchange-repository")
public class ExchangePersistenceAdapter implements ExchangeRepository {
    
    private final DatabaseExchangeRepository exchangeRepository;
    private final CategoryMapper categoryMapper;
    
    public ExchangePersistenceAdapter(
            DatabaseExchangeRepository exchangeRepository,
            CategoryMapper categoryMapper) {
        this.exchangeRepository = exchangeRepository;
        this.categoryMapper = categoryMapper;
    }
    
    @Override
    public Uni<List<Exchange>> findAll() {
        return exchangeRepository.findAllActive()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<List<Exchange>> search(String query) {
        return exchangeRepository.searchActive(query)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<Optional<Exchange>> findByCode(String code) {
        return exchangeRepository.findActiveByCode(code)
                .map(optEntity -> optEntity.map(categoryMapper::toExchange));
    }
    
    @Override
    public Uni<Optional<Exchange>> findById(Long id) {
        return exchangeRepository.findActiveById(id)
                .map(optEntity -> optEntity.map(categoryMapper::toExchange));
    }
    
    @Override
    public Uni<List<Exchange>> findByCountry(String country) {
        return exchangeRepository.findActiveByCountry(country)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<List<Exchange>> findByCurrencyCode(String currencyCode) {
        return exchangeRepository.findActiveByCurrencyCode(currencyCode)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<List<Exchange>> findUSExchanges() {
        return exchangeRepository.findUSExchanges()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<List<Exchange>> findEuropeanExchanges() {
        return exchangeRepository.findEuropeanExchanges()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<List<Exchange>> findAsianExchanges() {
        return exchangeRepository.findAsianExchanges()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toExchange)
                        .toList());
    }
    
    @Override
    public Uni<Long> count() {
        return exchangeRepository.countActive();
    }
}
