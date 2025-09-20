package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.infrastructure.adapters.incoming.web.dto.CurrencyResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API interface for Currency operations
 */
@Path("/v1/currencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Currencies", description = "API for managing and retrieving currency information")
public interface CurrencyController {

    @GET
    @Operation(
            summary = "Get all currencies",
            description = "Retrieve all available currencies with optional filtering"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Currencies retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                              "currencies": [
                                                {
                                                  "id": 1,
                                                  "code": "USD",
                                                  "name": "United States Dollar",
                                                  "symbol": "$",
                                                  "countryCode": "US",
                                                  "active": true
                                                }
                                              ],
                                              "count": 1
                                            }
                                            """
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Uni<Response> getCurrencies(
            @Parameter(
                    name = "search",
                    description = "Search term to filter currencies by code, name, or country code",
                    example = "USD"
            )
            @QueryParam("search")
            String search,

            @Parameter(
                    name = "countryCode",
                    description = "Filter currencies by specific country code",
                    example = "US"
            )
            @QueryParam("countryCode")
            String countryCode,

            @Parameter(
                    name = "majorOnly",
                    description = "Filter to show only major currencies (USD, EUR, JPY, GBP, etc.)",
                    schema = @Schema(type = SchemaType.BOOLEAN, defaultValue = "false"),
                    example = "false"
            )
            @QueryParam("majorOnly")
            Boolean majorOnly
    );

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get currency by ID",
            description = "Retrieve a specific currency by its ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Currency found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Currency not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Uni<Response> getCurrencyById(
            @Parameter(
                    name = "id",
                    description = "Currency ID",
                    required = true,
                    example = "1"
            )
            @PathParam("id")
            Long id
    );

    @GET
    @Path("/code/{code}")
    @Operation(
            summary = "Get currency by code",
            description = "Retrieve a specific currency by its code (e.g., USD, EUR)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Currency found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Currency not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid currency code format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Uni<Response> getCurrencyByCode(
            @Parameter(
                    name = "code",
                    description = "Currency code (3-letter ISO code)",
                    required = true,
                    example = "USD"
            )
            @PathParam("code")
            String code
    );
}
