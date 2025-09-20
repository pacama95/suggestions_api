package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetExchangeUseCase;
import com.portfolio.management.domain.port.incoming.GetExchangesUseCase;
import com.portfolio.management.domain.port.outgoing.ExchangeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

/**
 * Application service implementing exchange use cases
 */
@ApplicationScoped
public class ExchangeService implements GetExchangesUseCase, GetExchangeUseCase {

    private static final Logger LOG = Logger.getLogger(ExchangeService.class);

    private final ExchangeRepository repository;

    public ExchangeService(@Named("database-exchange-repository") ExchangeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Uni<GetExchangesUseCase.Result> execute(GetExchangesUseCase.Query query) {
        LOG.infof("Executing getExchanges: search=%s, country=%s, currency=%s, country=%s",
                query.searchTerm(), query.country(), query.currencyCode(), query.regionFilter());

        return performExchangesSearch(query)
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing getExchanges");
                    return new GetExchangesUseCase.Result.SystemError(
                            Errors.of("system", "An unexpected error occurred while retrieving exchanges", "SYSTEM_ERROR")
                    );
                });
    }

    @Override
    public Uni<GetExchangeUseCase.Result> execute(GetExchangeUseCase.Query query) {
        LOG.infof("Executing getExchange: id=%s, code=%s", query.id(), query.code());

        return performExchangeSearch(query)
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing getExchange");
                    return new GetExchangeUseCase.Result.SystemError(
                            Errors.of("system", "An unexpected error occurred while retrieving exchange", "SYSTEM_ERROR")
                    );
                });
    }

    private Uni<GetExchangesUseCase.Result> performExchangesSearch(GetExchangesUseCase.Query query) {
        Uni<java.util.List<com.portfolio.management.domain.model.Exchange>> exchangesUni;

        if (query.isRegionQuery()) {
            exchangesUni = switch (query.regionFilter()) {
                case US -> repository.findUSExchanges();
                case EUROPE -> repository.findEuropeanExchanges();
                case ASIA -> repository.findAsianExchanges();
            };
        } else if (query.isCountryQuery()) {
            exchangesUni = repository.findByCountry(query.country());
        } else if (query.isCurrencyQuery()) {
            exchangesUni = repository.findByCurrencyCode(query.currencyCode());
        } else if (query.isSearchQuery()) {
            exchangesUni = repository.search(query.searchTerm());
        } else {
            exchangesUni = repository.findAll();
        }

        return exchangesUni
                .map(exchanges -> {
                    LOG.infof("Found %d exchanges", exchanges.size());
                    return (GetExchangesUseCase.Result) new GetExchangesUseCase.Result.Success(exchanges, exchanges.size());
                });
    }

    private Uni<GetExchangeUseCase.Result> performExchangeSearch(GetExchangeUseCase.Query query) {
        Uni<java.util.Optional<com.portfolio.management.domain.model.Exchange>> exchangeUni;

        if (query.isIdQuery()) {
            exchangeUni = repository.findById(query.id());
        } else {
            exchangeUni = repository.findByCode(query.code());
        }

        return exchangeUni
                .map(optionalExchange -> {
                    if (optionalExchange.isPresent()) {
                        LOG.infof("Found exchange: %s", optionalExchange.get().code());
                        return (GetExchangeUseCase.Result) new GetExchangeUseCase.Result.Success(optionalExchange.get());
                    } else {
                        LOG.infof("Exchange not found");
                        return (GetExchangeUseCase.Result) new GetExchangeUseCase.Result.NotFound();
                    }
                });
    }
}
