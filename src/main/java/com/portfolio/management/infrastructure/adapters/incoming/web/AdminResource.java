package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Simple admin endpoints for managing stock data
 */
@ApplicationScoped
public class AdminResource implements AdminController {
    
    private final FetchAndStoreStockDataUseCase fetchAndStoreStockDataUseCase;

    public AdminResource(FetchAndStoreStockDataUseCase fetchAndStoreStockDataUseCase) {
        this.fetchAndStoreStockDataUseCase = fetchAndStoreStockDataUseCase;
    }

    @Override
    public Uni<Response> fetchStocks() {
        return fetchAndStoreStockDataUseCase.fetchAndStoreStocks()
                .map(result -> switch (result) {
                    case FetchAndStoreStockDataUseCase.Result.Success success->
                            Response.ok(Map.of("success", success.success(), "message", success.message(), "recordsProcessed", success.recordsProcessed())).build();
                    case FetchAndStoreStockDataUseCase.Result.Error error ->
                            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", error.message())).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(throwable, "Failed to fetch and store stocks.");
                    return Response.serverError().build();
                });
    }
}
