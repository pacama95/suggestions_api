package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.GetStockTypesUseCase;
import com.portfolio.management.domain.port.incoming.GetStockTypeUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.StockTypeWebMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST endpoint for StockType operations
 */
@ApplicationScoped
public class StockTypeResource implements StockTypeController {
    
    private static final Logger LOG = Logger.getLogger(StockTypeResource.class);
    
    private final GetStockTypesUseCase getStockTypesUseCase;
    private final GetStockTypeUseCase getStockTypeUseCase;
    private final StockTypeWebMapper stockTypeMapper;
    private final ErrorMapper errorMapper;

    public StockTypeResource(GetStockTypesUseCase getStockTypesUseCase,
                            GetStockTypeUseCase getStockTypeUseCase,
                            StockTypeWebMapper stockTypeMapper,
                            ErrorMapper errorMapper) {
        this.getStockTypesUseCase = getStockTypesUseCase;
        this.getStockTypeUseCase = getStockTypeUseCase;
        this.stockTypeMapper = stockTypeMapper;
        this.errorMapper = errorMapper;
    }

    @Override
    public Uni<Response> getStockTypes(String search) {
        
        LOG.infof("GET /stock-types - search: %s", search);
        
        GetStockTypesUseCase.Query query = new GetStockTypesUseCase.Query(search);
        
        return getStockTypesUseCase.execute(query)
            .map(result -> switch (result) {
                case GetStockTypesUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved %d stock types", success.count());
                    yield Response.ok(stockTypeMapper.toStockTypeResponse(success.stockTypes())).build();
                }
                case GetStockTypesUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetStockTypesUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }

    @Override
    public Uni<Response> getStockTypeById(Long id) {
        LOG.infof("GET /stock-types/%d", id);
        
        if (id == null) {
            LOG.warn("Stock type ID is required");
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorMapper.toErrorResponse("id", "Stock type ID is required", "MISSING_ID"))
                    .build()
            );
        }
        
        GetStockTypeUseCase.Query query = GetStockTypeUseCase.Query.byId(id);
        
        return getStockTypeUseCase.execute(query)
            .map(result -> switch (result) {
                case GetStockTypeUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved stock type: %s", success.stockType().code());
                    yield Response.ok(stockTypeMapper.toStockTypeResponse(success.stockType())).build();
                }
                case GetStockTypeUseCase.Result.NotFound notFound -> {
                    LOG.infof("Stock type not found for id: %d", id);
                    yield Response.status(Response.Status.NOT_FOUND)
                        .entity(errorMapper.toErrorResponse("stockType", "Stock type not found", "NOT_FOUND"))
                        .build();
                }
                case GetStockTypeUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetStockTypeUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }

    @Override
    public Uni<Response> getStockTypeByCode(String code) {
        LOG.infof("GET /stock-types/code/%s", code);
        
        if (code == null || code.trim().isEmpty()) {
            LOG.warn("Stock type code is required");
            return Uni.createFrom().item(
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorMapper.toErrorResponse("code", "Stock type code is required", "MISSING_CODE"))
                    .build()
            );
        }
        
        GetStockTypeUseCase.Query query = GetStockTypeUseCase.Query.byCode(code.trim().toUpperCase());
        
        return getStockTypeUseCase.execute(query)
            .map(result -> switch (result) {
                case GetStockTypeUseCase.Result.Success success -> {
                    LOG.infof("Successfully retrieved stock type: %s", success.stockType().code());
                    yield Response.ok(stockTypeMapper.toStockTypeResponse(success.stockType())).build();
                }
                case GetStockTypeUseCase.Result.NotFound notFound -> {
                    LOG.infof("Stock type not found for code: %s", code);
                    yield Response.status(Response.Status.NOT_FOUND)
                        .entity(errorMapper.toErrorResponse("stockType", "Stock type not found", "NOT_FOUND"))
                        .build();
                }
                case GetStockTypeUseCase.Result.ValidationError validationError -> {
                    LOG.warnf("Validation error: %s", validationError.errors());
                    yield Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMapper.toErrorResponse(validationError.errors()))
                        .build();
                }
                case GetStockTypeUseCase.Result.SystemError systemError -> {
                    LOG.errorf("System error: %s", systemError.errors());
                    yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(errorMapper.toErrorResponse(systemError.errors()))
                        .build();
                }
            });
    }
}
