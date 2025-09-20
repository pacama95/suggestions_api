package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API interface for Admin operations
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Administration", description = "Administrative operations for stock data management")
public interface AdminController {

    @POST
    @Path("/fetch-stocks")
    @Operation(
        summary = "Fetch stocks from TwelveData API and store in database",
        description = "Administrative endpoint to populate the database with stock data from the TwelveData API. " +
                     "This operation fetches all available stocks and stores them in the local H2 database. " +
                     "Use this endpoint to initialize or refresh the stock data."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Stocks fetched and stored successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success response",
                    summary = "Successful stock data import",
                    description = "Response when stocks are successfully fetched and stored",
                    value = """
                    {
                      "success": true,
                      "message": "Successfully fetched and stored 5000 stocks from TwelveData API",
                      "recordsProcessed": 5000
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500", 
            description = "Failed to fetch stocks",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Error response",
                    summary = "Failed to fetch stock data",
                    description = "Response when the stock data import fails",
                    value = """
                    {
                      "success": false,
                      "message": "Failed to fetch stocks: Connection timeout",
                      "recordsProcessed": 0
                    }
                    """
                )
            )
        )
    })
    Uni<Response> fetchStocks();
}
