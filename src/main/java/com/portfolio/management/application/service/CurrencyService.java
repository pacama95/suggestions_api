package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetCurrenciesUseCase;
import com.portfolio.management.domain.port.incoming.GetCurrencyUseCase;
import com.portfolio.management.domain.port.outgoing.CurrencyRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

/**
 * Application service implementing currency use cases
 */
@ApplicationScoped
public class CurrencyService implements GetCurrenciesUseCase, GetCurrencyUseCase {
    
    private static final Logger LOG = Logger.getLogger(CurrencyService.class);
    
    private final CurrencyRepository repository;

    public CurrencyService(@Named("database-currency-repository") CurrencyRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Uni<GetCurrenciesUseCase.Result> execute(GetCurrenciesUseCase.Query query) {
        LOG.infof("Executing getCurrencies: search=%s, country=%s, majorOnly=%s", 
                 query.searchTerm(), query.countryCode(), query.majorOnly());
        
        return performCurrenciesSearch(query)
            .onFailure().recoverWithItem(throwable -> {
                LOG.errorf(throwable, "Error executing getCurrencies");
                return new GetCurrenciesUseCase.Result.SystemError(
                    Errors.of("system", "An unexpected error occurred while retrieving currencies", "SYSTEM_ERROR")
                );
            });
    }
    
    @Override
    public Uni<GetCurrencyUseCase.Result> execute(GetCurrencyUseCase.Query query) {
        LOG.infof("Executing getCurrency: id=%s, code=%s", query.id(), query.code());
        
        return performCurrencySearch(query)
            .onFailure().recoverWithItem(throwable -> {
                LOG.errorf(throwable, "Error executing getCurrency");
                return new GetCurrencyUseCase.Result.SystemError(
                    Errors.of("system", "An unexpected error occurred while retrieving currency", "SYSTEM_ERROR")
                );
            });
    }
    
    private Uni<GetCurrenciesUseCase.Result> performCurrenciesSearch(GetCurrenciesUseCase.Query query) {
        Uni<java.util.List<com.portfolio.management.domain.model.Currency>> currenciesUni;
        
        if (query.majorOnly()) {
            currenciesUni = repository.findMajorCurrencies();
        } else if (query.isCountryQuery()) {
            currenciesUni = repository.findByCountryCode(query.countryCode());
        } else if (query.isSearchQuery()) {
            currenciesUni = repository.search(query.searchTerm());
        } else {
            currenciesUni = repository.findAll();
        }
        
        return currenciesUni
            .map(currencies -> {
                LOG.infof("Found %d currencies", currencies.size());
                return (GetCurrenciesUseCase.Result) new GetCurrenciesUseCase.Result.Success(currencies, currencies.size());
            });
    }
    
    private Uni<GetCurrencyUseCase.Result> performCurrencySearch(GetCurrencyUseCase.Query query) {
        Uni<java.util.Optional<com.portfolio.management.domain.model.Currency>> currencyUni;
        
        if (query.isIdQuery()) {
            currencyUni = repository.findById(query.id());
        } else {
            currencyUni = repository.findByCode(query.code());
        }
        
        return currencyUni
            .map(optionalCurrency -> {
                if (optionalCurrency.isPresent()) {
                    LOG.infof("Found currency: %s", optionalCurrency.get().code());
                    return (GetCurrencyUseCase.Result) new GetCurrencyUseCase.Result.Success(optionalCurrency.get());
                } else {
                    LOG.infof("Currency not found");
                    return (GetCurrencyUseCase.Result) new GetCurrencyUseCase.Result.NotFound();
                }
            });
    }
}
