package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.domain.port.outgoing.CurrencyRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseCurrencyRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for Currency repository following hexagonal architecture
 */
@ApplicationScoped
@Named("database-currency-repository")
public class CurrencyPersistenceAdapter implements CurrencyRepository {

    private final DatabaseCurrencyRepository currencyRepository;
    private final CategoryMapper categoryMapper;

    public CurrencyPersistenceAdapter(
            DatabaseCurrencyRepository currencyRepository,
            CategoryMapper categoryMapper) {
        this.currencyRepository = currencyRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @WithSession
    public Uni<List<Currency>> findAll() {
        return currencyRepository.findAllActive()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toCurrency)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<List<Currency>> search(String query) {
        return currencyRepository.searchActive(query)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toCurrency)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<Optional<Currency>> findByCode(String code) {
        return currencyRepository.findActiveByCode(code)
                .map(optEntity -> optEntity.map(categoryMapper::toCurrency));
    }

    @Override
    @WithSession
    public Uni<Optional<Currency>> findById(Long id) {
        return currencyRepository.findActiveById(id)
                .map(optEntity -> optEntity.map(categoryMapper::toCurrency));
    }

    @Override
    @WithSession
    public Uni<List<Currency>> findByCountryCode(String countryCode) {
        return currencyRepository.findActiveByCountryCode(countryCode)
                .map(entities -> entities.stream()
                        .map(categoryMapper::toCurrency)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<List<Currency>> findMajorCurrencies() {
        return currencyRepository.findMajorCurrencies()
                .map(entities -> entities.stream()
                        .map(categoryMapper::toCurrency)
                        .toList());
    }

    @Override
    @WithSession
    public Uni<Long> count() {
        return currencyRepository.countActive();
    }
}
