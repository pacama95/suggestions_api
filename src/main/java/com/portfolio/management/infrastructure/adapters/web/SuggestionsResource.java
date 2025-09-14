package com.portfolio.management.infrastructure.adapters.web;

import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.infrastructure.adapters.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.web.dto.SuggestionsResponse;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

/**
 * REST endpoint for ticker/symbol suggestions using reactive approach
 */
@Path("/suggestions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ticker Suggestions", description = "Reactive API for getting ticker/symbol suggestions")
public class SuggestionsResource {
    
    private static final Logger LOG = Logger.getLogger(SuggestionsResource.class);
    
    private final GetSuggestionsUseCase getSuggestionsUseCase;
    
    @Inject
    public SuggestionsResource(GetSuggestionsUseCase getSuggestionsUseCase) {
        this.getSuggestionsUseCase = getSuggestionsUseCase;
    }
    
    @GET
    @Operation(
        summary = "Search ticker suggestions",
        description = "Search for company ticker/symbol suggestions based on user input. " +
                     "Supports searching by ticker symbol (e.g., 'AAPL') or company name (e.g., 'Apple'). " +
                     "Optimized for frontend typeahead/autocomplete functionality with fast response times."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successful search operation",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
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
                              "region": "US",
                              "marketCap": "Large Cap",
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
                              "region": "US",
                              "marketCap": "Large Cap",
                              "currency": "USD"
                            },
                            {
                              "symbol": "META",
                              "name": "Meta Platforms Inc.",
                              "exchange": "NASDAQ",
                              "type": "Common Stock",
                              "region": "US",
                              "marketCap": "Large Cap",
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
                mediaType = MediaType.APPLICATION_JSON,
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
                mediaType = MediaType.APPLICATION_JSON,
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
    public Uni<Response> getSuggestions(
            @Parameter(
                description = "Search query for ticker symbol or company name. " +
                             "Examples: 'AAPL', 'Apple', 'Microsoft', 'MSFT'. " +
                             "Case-insensitive partial matching is supported.",
                required = true,
                example = "apple",
                schema = @Schema(type = SchemaType.STRING, minLength = 1, maxLength = 100)
            )
            @QueryParam("q") @NotBlank String query,
            
            @Parameter(
                description = "Maximum number of suggestions to return. " +
                             "Must be between 1 and 50. Default is 10 if not specified.",
                required = false,
                example = "10",
                schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "50", defaultValue = "10")
            )
            @QueryParam("limit") @Min(1) @Max(50) @DefaultValue("10") int limit) {
        
        LOG.debugf("Received suggestion request - query: %s, limit: %d", query, limit);
        
        var useCaseQuery = new GetSuggestionsUseCase.Query(query, limit);
        
        return getSuggestionsUseCase.execute(useCaseQuery)
            .onItem().transform(this::mapToHttpResponse)
            .onFailure().recoverWithItem(throwable -> {
                LOG.errorf(throwable, "Unexpected error processing suggestion request");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of("An unexpected error occurred"))
                    .build();
            });
    }
    
    private Response mapToHttpResponse(GetSuggestionsUseCase.Result result) {
        return switch (result) {
            case GetSuggestionsUseCase.Result.Success(var suggestions, var query, var count) -> {
                var response = new SuggestionsResponse(
                    suggestions.stream()
                        .map(suggestion -> new SuggestionsResponse.TickerSuggestionDto(
                            suggestion.symbol(),
                            suggestion.name(),
                            suggestion.exchange(),
                            suggestion.type(),
                            suggestion.region(),
                            suggestion.marketCap(),
                            suggestion.currency()
                        ))
                        .collect(Collectors.toList()),
                    query,
                    count
                );
                yield Response.ok(response).build();
            }
            
            case GetSuggestionsUseCase.Result.ValidationError(var errors) -> {
                var errorDetails = errors.errors().stream()
                    .map(error -> new ErrorResponse.ErrorDetail(
                        error.field(),
                        error.message(),
                        error.code()
                    ))
                    .collect(Collectors.toList());
                
                var errorResponse = ErrorResponse.of(errorDetails, "Validation failed");
                yield Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
            }
            
            case GetSuggestionsUseCase.Result.SystemError(var errors) -> {
                var errorDetails = errors.errors().stream()
                    .map(error -> new ErrorResponse.ErrorDetail(
                        error.field(),
                        error.message(),
                        error.code()
                    ))
                    .collect(Collectors.toList());
                
                var errorResponse = ErrorResponse.of(errorDetails, "System error occurred");
                yield Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
            }
        };
    }
}
