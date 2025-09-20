package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.GetCurrenciesUseCase;
import com.portfolio.management.domain.port.incoming.GetCurrencyUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.CurrencyWebMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST endpoint for Currency operations
 */
@ApplicationScoped
public class CurrencyResource implements CurrencyController {
    
    private static final Logger LOG = Logger.getLogger(CurrencyResource.class);
    
    private final GetCurrenciesUseCase getCurrenciesUseCase;
    private final GetCurrencyUseCase getCurrencyUseCase;
    private final CurrencyWebMapper currencyMapper;
    private final ErrorMapper errorMapper;

    public CurrencyResource(GetCurrenciesUseCase getCurrenciesUseCase,
                           GetCurrencyUseCase getCurrencyUseCase,
                           CurrencyWebMapper currencyMapper,
                           ErrorMapper errorMapper) {
        this.getCurrenciesUseCase = getCurrenciesUseCase;
        this.getCurrencyUseCase = getCurrencyUseCase;
        this.currencyMapper = currencyMapper;
        this.errorMapper = errorMapper;
    }

    @Override
    public Uni<Response> getCurrencies(String search,
                                      String countryCode,
                                      Boolean majorOnly) {
        
        LOG.infof("GET /currencies - search: %s, countryCode: %s, majorOnly: %s", search, countryCode, majorOnly);
        
        GetCurrenciesUseCase.Query query = new GetCurrenciesUseCase.Query(
            search, 
            countryCode, 
            majorOnly != null && majorOnly
        );
        
        return getCurrenciesUseCase.execute(query)
            .map(result -> switch (result) {
                case GetCurrenciesUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved %d currencies", success.count());
                    yield Response.ok(currencyMapper.toCurrencyResponse(success.currencies())).build();
                }
                case GetCurrenciesUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetCurrenciesUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }

    @Override
    public Uni<Response> getCurrencyById(Long id) {
        LOG.infof("GET /currencies/%d", id);
        
        if (id == null) {
            LOG.warn("Currency ID is required");
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorMapper.toErrorResponse("id", "Currency ID is required", "MISSING_ID"))
                    .build()
            );
        }
        
        GetCurrencyUseCase.Query query = GetCurrencyUseCase.Query.byId(id);
        
        return getCurrencyUseCase.execute(query)
            .map(result -> switch (result) {
                case GetCurrencyUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved currency: %s", success.currency().code());
                    yield Response.ok(currencyMapper.toCurrencyResponse(success.currency())).build();
                }
                case GetCurrencyUseCase.Result.NotFound notFound -> {
                    LOG.infof("Currency not found for id: %d", id);
                    yield Response.status(Response.Status.NOT_FOUND)
                        .entity(errorMapper.toErrorResponse("currency", "Currency not found", "NOT_FOUND"))
                        .build();
                }
                case GetCurrencyUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetCurrencyUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }

    @Override
    public Uni<Response> getCurrencyByCode(String code) {
        LOG.infof("GET /currencies/code/%s", code);
        
        if (code == null || code.trim().isEmpty()) {
            LOG.warn("Currency code is required");
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorMapper.toErrorResponse("code", "Currency code is required", "MISSING_CODE"))
                    .build()
            );
        }
        
        GetCurrencyUseCase.Query query = GetCurrencyUseCase.Query.byCode(code.trim().toUpperCase());
        
        return getCurrencyUseCase.execute(query)
            .map(result -> switch (result) {
                case GetCurrencyUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved currency: %s", success.currency().code());
                    yield Response.ok(currencyMapper.toCurrencyResponse(success.currency())).build();
                }
                case GetCurrencyUseCase.Result.NotFound notFound -> {
                    LOG.infof("Currency not found for code: %s", code);
                    yield Response.status(Response.Status.NOT_FOUND)
                        .entity(errorMapper.toErrorResponse("currency", "Currency not found", "NOT_FOUND"))
                        .build();
                }
                case GetCurrencyUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetCurrencyUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }
}
