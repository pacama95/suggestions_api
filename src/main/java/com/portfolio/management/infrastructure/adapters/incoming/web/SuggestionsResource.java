package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.SuggestionsResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.StockMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * REST endpoint for ticker/symbol suggestions
 */
@ApplicationScoped
public class SuggestionsResource implements SuggestionsController {

    private static final Logger LOG = Logger.getLogger(SuggestionsResource.class);

    private final GetSuggestionsUseCase getSuggestionsUseCase;
    private final GetSuggestionsAdvancedUseCase getSuggestionsAdvancedUseCase;
    private final StockMapper suggestionMapper;
    private final ErrorMapper errorMapper;

    public SuggestionsResource(GetSuggestionsUseCase getSuggestionsUseCase,
                               GetSuggestionsAdvancedUseCase getSuggestionsAdvancedUseCase,
                               StockMapper suggestionMapper,
                               ErrorMapper errorMapper) {
        this.getSuggestionsUseCase = getSuggestionsUseCase;
        this.getSuggestionsAdvancedUseCase = getSuggestionsAdvancedUseCase;
        this.suggestionMapper = suggestionMapper;
        this.errorMapper = errorMapper;
    }

    @Override
    public Uni<Response> getSuggestions(String query, int limit) {
        LOG.infof("Received suggestion request - query: %s, limit: %d", query, limit);

        return Uni.createFrom().item(() -> new GetSuggestionsUseCase.Query(query, limit))
                .flatMap(getSuggestionsUseCase::execute)
                .onItem().transform(result -> switch (result) {
                            case GetSuggestionsUseCase.Result.Success success -> mapToHttpResponse(success);
                            case GetSuggestionsUseCase.Result.SystemError systemError -> mapToHttpResponse(systemError);
                            case GetSuggestionsUseCase.Result.ValidationError validationError ->
                                    mapToHttpResponse(validationError);
                        }
                );
    }

    @Override
    public Uni<Response> advancedSearch(String symbol, String companyName, String exchange, String country, String currency, int limit) {
        // Validate at least one search parameter is provided
        if (isBlank(symbol) && isBlank(companyName) && isBlank(exchange) && isBlank(country) && isBlank(currency)) {
            var errorResponse = ErrorResponse.of("At least one search parameter must be provided");
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build());
        }

        LOG.infof("Advanced search request - symbol: %s, companyName: %s, exchange: %s, country: %s, currency: %s, limit: %d",
                symbol, companyName, exchange, country, currency, limit);

        return Uni.createFrom().item(() -> new GetSuggestionsAdvancedUseCase.Query(symbol, companyName, exchange, country, currency, limit))
                .flatMap(getSuggestionsAdvancedUseCase::execute)
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
                        suggestionMapper.toTickerSuggestionDtoList(suggestions),
                        query,
                        count
                );
                yield Response.ok(response).build();
            }

            case GetSuggestionsUseCase.Result.ValidationError(var errors) -> {
                var errorDetails = errorMapper.toErrorDetailList(errors.errors());
                var errorResponse = ErrorResponse.of(errorDetails, "Validation failed");
                yield Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
            }

            case GetSuggestionsUseCase.Result.SystemError(var errors) -> {
                var errorDetails = errorMapper.toErrorDetailList(errors.errors());
                var errorResponse = ErrorResponse.of(errorDetails, "System error occurred");
                yield Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
            }
        };
    }


    private Response mapToHttpResponse(GetSuggestionsAdvancedUseCase.Result result) {
        return switch (result) {
            case GetSuggestionsAdvancedUseCase.Result.Success(var suggestions, var query, var count) -> {
                var response = new SuggestionsResponse(
                        suggestionMapper.toTickerSuggestionDtoList(suggestions),
                        query,
                        count
                );
                yield Response.ok(response).build();
            }

            case GetSuggestionsAdvancedUseCase.Result.ValidationError(var errors) -> {
                var errorDetails = errorMapper.toErrorDetailList(errors.errors());
                var errorResponse = ErrorResponse.of(errorDetails, "Validation failed");
                yield Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
            }

            case GetSuggestionsAdvancedUseCase.Result.SystemError(var errors) -> {
                var errorDetails = errorMapper.toErrorDetailList(errors.errors());
                var errorResponse = ErrorResponse.of(errorDetails, "System error occurred");
                yield Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
            }
        };
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
