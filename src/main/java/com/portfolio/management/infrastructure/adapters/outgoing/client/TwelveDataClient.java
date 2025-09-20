package com.portfolio.management.infrastructure.adapters.outgoing.client;

import com.portfolio.management.infrastructure.adapters.outgoing.client.dto.TwelveDataStockResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for TwelveData API
 */
@RegisterRestClient(configKey = "twelve-data-api")
public interface TwelveDataClient {

    @GET
    @Path("/stocks")
    Uni<TwelveDataStockResponse> getAllStocks(@QueryParam("apikey") String apiKey);
}
