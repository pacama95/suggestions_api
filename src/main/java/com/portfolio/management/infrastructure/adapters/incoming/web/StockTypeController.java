package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.StockTypeResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API interface for StockType operations
 */
@Path("/v1/stock-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Stock Types", description = "API for managing and retrieving stock type information")
public interface StockTypeController {

    @GET
    @Operation(
            summary = "Get all stock types",
            description = "Retrieve all available stock types with optional filtering"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Stock types retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StockTypeResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                              "stockTypes": [
                                                {
                                                  "id": 1,
                                                  "code": "CS",
                                                  "name": "Common Stock",
                                                  "description": "Ordinary shares that represent ownership in a company",
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
    Uni<Response> getStockTypes(
            @Parameter(
                    name = "search",
                    description = "Search term to filter stock types by code or name",
                    example = "Common"
            )
            @QueryParam("search")
            String search
    );

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get stock type by ID",
            description = "Retrieve a specific stock type by its ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Stock type found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StockTypeResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Stock type not found",
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
    Uni<Response> getStockTypeById(
            @Parameter(
                    name = "id",
                    description = "Stock type ID",
                    required = true,
                    example = "1"
            )
            @PathParam("id")
            Long id
    );

    @GET
    @Path("/code/{code}")
    @Operation(
            summary = "Get stock type by code",
            description = "Retrieve a specific stock type by its code (e.g., CS, PS)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Stock type found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StockTypeResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Stock type not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid stock type code format",
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
    Uni<Response> getStockTypeByCode(
            @Parameter(
                    name = "code",
                    description = "Stock type code",
                    required = true,
                    example = "CS"
            )
            @PathParam("code")
            String code
    );
}
