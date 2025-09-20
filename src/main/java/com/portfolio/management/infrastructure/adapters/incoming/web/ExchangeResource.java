package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.GetExchangeUseCase;
import com.portfolio.management.domain.port.incoming.GetExchangesUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ExchangeWebMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST endpoint for Exchange operations
 */
@ApplicationScoped
public class ExchangeResource implements ExchangeController {

    private static final Logger LOG = Logger.getLogger(ExchangeResource.class);

    private final GetExchangesUseCase getExchangesUseCase;
    private final GetExchangeUseCase getExchangeUseCase;
    private final ExchangeWebMapper exchangeMapper;
    private final ErrorMapper errorMapper;

    public ExchangeResource(GetExchangesUseCase getExchangesUseCase,
                            GetExchangeUseCase getExchangeUseCase,
                            ExchangeWebMapper exchangeMapper,
                            ErrorMapper errorMapper) {
        this.getExchangesUseCase = getExchangesUseCase;
        this.getExchangeUseCase = getExchangeUseCase;
        this.exchangeMapper = exchangeMapper;
        this.errorMapper = errorMapper;
    }

    @Override
    public Uni<Response> getExchanges(String search,
                                      String country,
                                      String currency,
                                      String region) {

        LOG.infof("GET /exchanges - search: %s, country: %s, currency: %s, country: %s",
                search, country, currency, region);

        GetExchangesUseCase.RegionFilter regionFilter = null;
        if (region != null && !region.trim().isEmpty()) {
            try {
                regionFilter = GetExchangesUseCase.RegionFilter.valueOf(region.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                LOG.warnf("Invalid country filter: %s", region);
                return Uni.createFrom().item(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMapper.toErrorResponse("country", "Invalid country. Valid values: US, EUROPE, ASIA", "INVALID_REGION"))
                                .build()
                );
            }
        }

        GetExchangesUseCase.Query query = new GetExchangesUseCase.Query(search, country, currency, regionFilter);

        return getExchangesUseCase.execute(query)
                .map(result -> switch (result) {
                    case GetExchangesUseCase.Result.Success success -> {
                        LOG.infof("Successfully retrieved %d exchanges", success.count());
                        yield Response.ok(exchangeMapper.toExchangeResponse(success.exchanges())).build();
                    }
                    case GetExchangesUseCase.Result.ValidationError validationError -> {
                        LOG.warnf("Validation error: %s", validationError.errors());
                        yield Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMapper.toErrorResponse(validationError.errors()))
                                .build();
                    }
                    case GetExchangesUseCase.Result.SystemError systemError -> {
                        LOG.errorf("System error: %s", systemError.errors());
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(errorMapper.toErrorResponse(systemError.errors()))
                                .build();
                    }
                });
    }

    @Override
    public Uni<Response> getExchangeById(Long id) {
        LOG.infof("GET /exchanges/%d", id);

        if (id == null) {
            LOG.warn("Exchange ID is required");
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorMapper.toErrorResponse("id", "Exchange ID is required", "MISSING_ID"))
                            .build()
            );
        }

        GetExchangeUseCase.Query query = GetExchangeUseCase.Query.byId(id);

        return getExchangeUseCase.execute(query)
                .map(result -> switch (result) {
                    case GetExchangeUseCase.Result.Success success -> {
                        LOG.infof("Successfully retrieved exchange: %s", success.exchange().code());
                        yield Response.ok(exchangeMapper.toExchangeResponse(success.exchange())).build();
                    }
                    case GetExchangeUseCase.Result.NotFound notFound -> {
                        LOG.infof("Exchange not found for id: %d", id);
                        yield Response.status(Response.Status.NOT_FOUND)
                                .entity(errorMapper.toErrorResponse("exchange", "Exchange not found", "NOT_FOUND"))
                                .build();
                    }
                    case GetExchangeUseCase.Result.ValidationError validationError -> {
                        LOG.warnf("Validation error: %s", validationError.errors());
                        yield Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMapper.toErrorResponse(validationError.errors()))
                                .build();
                    }
                    case GetExchangeUseCase.Result.SystemError systemError -> {
                        LOG.errorf("System error: %s", systemError.errors());
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(errorMapper.toErrorResponse(systemError.errors()))
                                .build();
                    }
                });
    }

    @Override
    public Uni<Response> getExchangeByCode(String code) {
        LOG.infof("GET /exchanges/code/%s", code);

        if (code == null || code.trim().isEmpty()) {
            LOG.warn("Exchange code is required");
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorMapper.toErrorResponse("code", "Exchange code is required", "MISSING_CODE"))
                            .build()
            );
        }

        GetExchangeUseCase.Query query = GetExchangeUseCase.Query.byCode(code.trim().toUpperCase());

        return getExchangeUseCase.execute(query)
                .map(result -> switch (result) {
                    case GetExchangeUseCase.Result.Success success -> {
                        LOG.infof("Successfully retrieved exchange: %s", success.exchange().code());
                        yield Response.ok(exchangeMapper.toExchangeResponse(success.exchange())).build();
                    }
                    case GetExchangeUseCase.Result.NotFound notFound -> {
                        LOG.infof("Exchange not found for code: %s", code);
                        yield Response.status(Response.Status.NOT_FOUND)
                                .entity(errorMapper.toErrorResponse("exchange", "Exchange not found", "NOT_FOUND"))
                                .build();
                    }
                    case GetExchangeUseCase.Result.ValidationError validationError -> {
                        LOG.warnf("Validation error: %s", validationError.errors());
                        yield Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMapper.toErrorResponse(validationError.errors()))
                                .build();
                    }
                    case GetExchangeUseCase.Result.SystemError systemError -> {
                        LOG.errorf("System error: %s", systemError.errors());
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(errorMapper.toErrorResponse(systemError.errors()))
                                .build();
                    }
                });
    }
}
