package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.SuggestionsResponse;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
 * REST API interface for Ticker Suggestions operations
 */
@Path("/v1/suggestions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ticker Suggestions", description = "Reactive API for getting ticker/symbol suggestions")
public interface SuggestionsController {

    @Operation(
            summary = "Search ticker suggestions",
            description = "Search for company ticker/symbol suggestions based on user input. " +
                    "Supports searching by ticker symbol (e.g., 'AAPL') or company name (e.g., 'Apple'). " +
                    "Optimized for frontend typeahead/autocomplete functionality with fast response times."
    )
    @GET
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Successful search operation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuggestionsResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Apple search",
                                            summary = "Search for Apple Inc.",
                                            description = "Example response when searching for 'apple'",
                                            value = """
                                                    {
                                                      "suggestions": [
                                                        {
                                                          "symbol": "AAPL",
                                                          "name": "Apple Inc.",
                                                          "exchange": "NASDAQ",
                                                          "type": "Common Stock",
                                                          "country": "US",
                                                          "currency": "USD"
                                                        }
                                                      ],
                                                      "query": "apple",
                                                      "count": 1
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Multiple results",
                                            summary = "Multiple ticker results",
                                            description = "Example response with multiple matching tickers",
                                            value = """
                                                    {
                                                      "suggestions": [
                                                        {
                                                          "symbol": "MSFT",
                                                          "name": "Microsoft Corporation",
                                                          "exchange": "NASDAQ",
                                                          "type": "Common Stock",
                                                          "country": "US",
                                                          "currency": "USD"
                                                        },
                                                        {
                                                          "symbol": "META",
                                                          "name": "Meta Platforms Inc.",
                                                          "exchange": "NASDAQ",
                                                          "type": "Common Stock",
                                                          "country": "US",
                                                          "currency": "USD"
                                                        }
                                                      ],
                                                      "query": "tech",
                                                      "count": 2
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "No results",
                                            summary = "No matching tickers found",
                                            description = "Example response when no tickers match the search query",
                                            value = """
                                                    {
                                                      "suggestions": [],
                                                      "query": "nonexistent",
                                                      "count": 0
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Bad request - validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Empty query",
                                            summary = "Empty search query",
                                            description = "Error response when search query is empty or blank",
                                            value = """
                                                    {
                                                      "errors": [
                                                        {
                                                          "field": "input",
                                                          "message": "Search input cannot be empty",
                                                          "code": "EMPTY_INPUT"
                                                        }
                                                      ],
                                                      "message": "Validation failed",
                                                      "timestamp": 1694771234567
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid limit",
                                            summary = "Invalid limit parameter",
                                            description = "Error response when limit parameter is out of valid range",
                                            value = """
                                                    {
                                                      "errors": [
                                                        {
                                                          "field": "limit",
                                                          "message": "Limit must be between 1 and 50",
                                                          "code": "INVALID_LIMIT"
                                                        }
                                                      ],
                                                      "message": "Validation failed",
                                                      "timestamp": 1694771234567
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "System error",
                                    summary = "Internal system error",
                                    description = "Error response when an unexpected system error occurs",
                                    value = """
                                            {
                                              "errors": [
                                                {
                                                  "field": "system",
                                                  "message": "An unexpected error occurred during suggestion search",
                                                  "code": "SYSTEM_ERROR"
                                                }
                                              ],
                                              "message": "System error occurred",
                                              "timestamp": 1694771234567
                                            }
                                            """
                            )
                    )
            )
    })
    Uni<Response> getSuggestions(
            @Parameter(
                    description = "Search query for ticker symbol or company name. " +
                            "Examples: 'AAPL', 'Apple', 'Microsoft', 'MSFT'. " +
                            "Case-insensitive partial matching is supported.",
                    required = true,
                    example = "apple",
                    schema = @Schema(type = SchemaType.STRING, minLength = 1, maxLength = 100)
            )
            @QueryParam("q")
            String query,

            @Parameter(
                    description = "Maximum number of suggestions to return. " +
                            "Must be between 1 and 50. Default is 10 if not specified.",
                    required = false,
                    example = "10",
                    schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "50", defaultValue = "10")
            )
            @QueryParam("limit")
            @Min(1)
            @Max(50)
            @DefaultValue("10")
            int limit
    );

    @Operation(
            summary = "Advanced stock search",
            description = "Search for stocks with multiple optional criteria. " +
                    "All parameters are optional - you can search by any combination of symbol, company name, exchange, country, or currency. " +
                    "Performs partial matching on all text fields."
    )
    @GET
    @Path("/search")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Successful advanced search operation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuggestionsResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Search by symbol",
                                            summary = "Search by partial symbol match",
                                            description = "Example searching for stocks with 'AA' in the symbol",
                                            value = """
                                                    {
                                                      "suggestions": [
                                                        {
                                                          "symbol": "AAPL",
                                                          "name": "Apple Inc.",
                                                          "exchange": "NASDAQ",
                                                          "type": "Common Stock",
                                                          "country": "US",
                                                          "currency": "USD"
                                                        }
                                                      ],
                                                      "query": "Advanced search symbol:AA",
                                                      "count": 1
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Search by exchange",
                                            summary = "Search by exchange",
                                            description = "Example searching for NASDAQ stocks",
                                            value = """
                                                    {
                                                      "suggestions": [
                                                        {
                                                          "symbol": "MSFT",
                                                          "name": "Microsoft Corporation",
                                                          "exchange": "NASDAQ",
                                                          "type": "Common Stock",
                                                          "country": "US",
                                                          "currency": "USD"
                                                        }
                                                      ],
                                                      "query": "Advanced search exchange:NASDAQ",
                                                      "count": 1
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Bad request - validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "No search parameters",
                                    summary = "No search parameters provided",
                                    description = "Error when no search parameters are specified",
                                    value = """
                                            {
                                              "errors": [],
                                              "message": "At least one search parameter must be provided",
                                              "timestamp": 1694771234567
                                            }
                                            """
                            )
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
    Uni<Response> advancedSearch(
            @Parameter(
                    description = "Partial symbol to search for (e.g., 'AA', 'MSFT'). Case-insensitive partial matching.",
                    required = false,
                    example = "AAPL"
            )
            @QueryParam("symbol")
            String symbol,

            @Parameter(
                    description = "Partial company name to search for (e.g., 'Apple', 'Microsoft'). Case-insensitive partial matching.",
                    required = false,
                    example = "Apple"
            )
            @QueryParam("companyName")
            String companyName,

            @Parameter(
                    description = "Exchange to search for (e.g., 'NASDAQ', 'NYSE'). Case-insensitive partial matching.",
                    required = false,
                    example = "NASDAQ"
            )
            @QueryParam("exchange")
            String exchange,

            @Parameter(
                    description = "Region/country to search for (e.g., 'US', 'United States'). Case-insensitive partial matching.",
                    required = false,
                    example = "US"
            )
            @QueryParam("country")
            String country,

            @Parameter(
                    description = "Currency to search for (e.g., 'USD', 'EUR'). Case-insensitive partial matching.",
                    required = false,
                    example = "USD"
            )
            @QueryParam("currency")
            String currency,

            @Parameter(
                    description = "Maximum number of results to return. Must be between 1 and 100.",
                    required = false,
                    schema = @Schema(
                            type = SchemaType.INTEGER,
                            minimum = "1",
                            maximum = "100",
                            defaultValue = "20"
                    ),
                    example = "20"
            )
            @QueryParam("limit")
            @Min(1)
            @Max(50)
            @DefaultValue("10")
            int limit
    );
}
