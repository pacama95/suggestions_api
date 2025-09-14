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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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
    @Operation(summary = "Get ticker suggestions", 
               description = "Get company ticker/symbol suggestions based on user input. Optimized for frontend typeahead functionality.")
    public Uni<Response> getSuggestions(
            @Parameter(description = "Search query (ticker symbol or company name)", required = true)
            @QueryParam("q") @NotBlank String query,
            @Parameter(description = "Maximum number of results to return (1-50, default: 10)")
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
