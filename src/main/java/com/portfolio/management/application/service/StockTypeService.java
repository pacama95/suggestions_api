package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetStockTypeUseCase;
import com.portfolio.management.domain.port.incoming.GetStockTypesUseCase;
import com.portfolio.management.domain.port.outgoing.StockTypeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

/**
 * Application service implementing stock type use cases
 */
@ApplicationScoped
public class StockTypeService implements GetStockTypesUseCase, GetStockTypeUseCase {

    private static final Logger LOG = Logger.getLogger(StockTypeService.class);

    private final StockTypeRepository repository;

    public StockTypeService(@Named("database-stocktype-repository") StockTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Uni<GetStockTypesUseCase.Result> execute(GetStockTypesUseCase.Query query) {
        LOG.infof("Executing getStockTypes: search=%s", query.searchTerm());

        return performStockTypesSearch(query)
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing getStockTypes");
                    return new GetStockTypesUseCase.Result.SystemError(
                            Errors.of("system", "An unexpected error occurred while retrieving stock types", "SYSTEM_ERROR")
                    );
                });
    }

    @Override
    public Uni<GetStockTypeUseCase.Result> execute(GetStockTypeUseCase.Query query) {
        LOG.infof("Executing getStockType: id=%s, code=%s", query.id(), query.code());

        return performStockTypeSearch(query)
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing getStockType");
                    return new GetStockTypeUseCase.Result.SystemError(
                            Errors.of("system", "An unexpected error occurred while retrieving stock type", "SYSTEM_ERROR")
                    );
                });
    }

    private Uni<GetStockTypesUseCase.Result> performStockTypesSearch(GetStockTypesUseCase.Query query) {
        Uni<java.util.List<com.portfolio.management.domain.model.StockType>> stockTypesUni;

        if (query.isSearchQuery()) {
            stockTypesUni = repository.search(query.searchTerm());
        } else {
            stockTypesUni = repository.findAll();
        }

        return stockTypesUni
                .map(stockTypes -> {
                    LOG.infof("Found %d stock types", stockTypes.size());
                    return new GetStockTypesUseCase.Result.Success(stockTypes, stockTypes.size());
                });
    }

    private Uni<GetStockTypeUseCase.Result> performStockTypeSearch(GetStockTypeUseCase.Query query) {
        Uni<java.util.Optional<com.portfolio.management.domain.model.StockType>> stockTypeUni;

        if (query.isIdQuery()) {
            stockTypeUni = repository.findById(query.id());
        } else {
            stockTypeUni = repository.findByCode(query.code());
        }

        return stockTypeUni
                .map(optionalStockType -> {
                    if (optionalStockType.isPresent()) {
                        LOG.infof("Found stock type: %s", optionalStockType.get().code());
                        return (GetStockTypeUseCase.Result) new GetStockTypeUseCase.Result.Success(optionalStockType.get());
                    } else {
                        LOG.infof("Stock type not found");
                        return (GetStockTypeUseCase.Result) new GetStockTypeUseCase.Result.NotFound();
                    }
                });
    }
}
