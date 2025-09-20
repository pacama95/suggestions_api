package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Application service implementing the GetSuggestionsUseCase
 */
@ApplicationScoped
public class SuggestionService implements GetSuggestionsUseCase {

    private static final Logger LOG = Logger.getLogger(SuggestionService.class);

    private final StockPort stockPort;

    public SuggestionService(StockPort stockPort) {
        this.stockPort = stockPort;
    }

    @Override
    public Uni<Result> execute(Query query) {
        return validateQuery(query)
                .onItem().transformToUni(validationResult -> {
                    if (validationResult != null) {
                        LOG.errorf("Not valid search query: '%s' with limit: '%d'", query.input(), query.limit());
                        return Uni.createFrom().item(validationResult);
                    }

                    LOG.infof("Executing suggestion search for query: %s, limit: %d", query.input(), query.limit());
                    return searchSuggestions(query);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing suggestion search for query: %s", query.input());
                    return new Result.SystemError(
                            Errors.of("system", "An unexpected error occurred during suggestion search", "SYSTEM_ERROR")
                    );
                });
    }

    private Uni<Result> validateQuery(Query query) {
        return Uni.createFrom().item(() -> {
            if (query.input().trim().isEmpty()) {
                return new Result.ValidationError(
                        Errors.of("input", "Search input cannot be empty", "EMPTY_INPUT")
                );
            }

            // No validation errors
            return null;
        });
    }

    private Uni<Result> searchSuggestions(Query query) {
        return stockPort.findActiveByQuery(query.input().trim(), query.limit())
                .onItem().transform(stocks -> {
                    LOG.infof("Found %d suggestions for query: %s", stocks.size(), query.input());
                    return (Result) new Result.Success(stocks, query.input(), stocks.size());
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Repository error for query: %s", query.input());
                    return new Result.SystemError(
                            Errors.of("repository", "Failed to retrieve suggestions", "REPOSITORY_ERROR")
                    );
                });
    }
}
