package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ExchangeResponse;
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
 * REST API interface for Exchange operations
 */
@Path("/v1/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Exchanges", description = "API for managing and retrieving stock exchange information")
public interface ExchangeController {

    @GET
    @Operation(
            summary = "Get all exchanges",
            description = "Retrieve all available stock exchanges with optional filtering"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Exchanges retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                              "exchanges": [
                                                {
                                                  "id": 1,
                                                  "code": "NYSE",
                                                  "name": "New York Stock Exchange",
                                                  "country": "United States",
                                                  "timezone": "America/New_York",
                                                  "currencyCode": "USD",
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
    Uni<Response> getExchanges(
            @Parameter(
                    name = "search",
                    description = "Search term to filter exchanges by code, name, or country",
                    example = "NYSE"
            )
            @QueryParam("search")
            String search,

            @Parameter(
                    name = "country",
                    description = "Filter exchanges by specific country",
                    example = "United States"
            )
            @QueryParam("country")
            String country,

            @Parameter(
                    name = "currency",
                    description = "Filter exchanges by currency code",
                    example = "USD"
            )
            @QueryParam("currency")
            String currency,

            @Parameter(
                    name = "country",
                    description = "Filter exchanges by country",
                    schema = @Schema(
                            type = SchemaType.STRING,
                            enumeration = {"US", "EUROPE", "ASIA"}
                    ),
                    example = "US"
            )
            @QueryParam("country")
            String region
    );

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get exchange by ID",
            description = "Retrieve a specific exchange by its ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Exchange found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Exchange not found",
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
    Uni<Response> getExchangeById(
            @Parameter(
                    name = "id",
                    description = "Exchange ID",
                    required = true,
                    example = "1"
            )
            @PathParam("id")
            Long id
    );

    @GET
    @Path("/code/{code}")
    @Operation(
            summary = "Get exchange by code",
            description = "Retrieve a specific exchange by its code (e.g., NYSE, NASDAQ)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Exchange found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Exchange not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid exchange code format",
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
    Uni<Response> getExchangeByCode(
            @Parameter(
                    name = "code",
                    description = "Exchange code",
                    required = true,
                    example = "NYSE"
            )
            @PathParam("code")
            String code
    );
}
