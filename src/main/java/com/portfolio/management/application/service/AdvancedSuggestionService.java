package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Application service implementing the GetSuggestionsAdvancedUseCase
 */
@ApplicationScoped
public class AdvancedSuggestionService implements GetSuggestionsAdvancedUseCase {

    private static final Logger LOG = Logger.getLogger(AdvancedSuggestionService.class);

    private final StockPort stockPort;

    public AdvancedSuggestionService(StockPort stockPort) {
        this.stockPort = stockPort;
    }

    @Override
    public Uni<Result> execute(Query query) {
        LOG.infof("Executing advanced suggestion search: %s, limit: %d", query.getSearchDescription(), query.limit());

        return validateQuery(query)
                .onItem().transformToUni(validationResult -> {
                    if (validationResult != null) {
                        return Uni.createFrom().item(validationResult);
                    }

                    return searchSuggestions(query);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Error executing advanced suggestion search: %s", query.getSearchDescription());
                    return new Result.SystemError(
                            Errors.of("system", "An unexpected error occurred during advanced suggestion search", "SYSTEM_ERROR")
                    );
                });
    }

    private Uni<Result> validateQuery(Query query) {
        return Uni.createFrom().item(() -> {
            if (!query.hasSearchCriteria()) {
                return new Result.ValidationError(
                        Errors.of("search_criteria", "At least one search parameter must be provided", "NO_SEARCH_CRITERIA")
                );
            }

            return null;
        });
    }

    private Uni<Result> searchSuggestions(Query query) {
        return stockPort.findByAdvancedSearch(
                        query.symbol(),
                        query.companyName(),
                        query.exchange(),
                        query.country(),
                        query.currency(),
                        query.limit())
                .onItem().transform(suggestions -> {
                    LOG.infof("Found %d suggestions for advanced search: %s", suggestions.size(), query.getSearchDescription());
                    return (Result) new Result.Success(suggestions, query.getSearchDescription(), suggestions.size());
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Repository error for advanced search: %s", query.getSearchDescription());
                    return (Result) new Result.SystemError(
                            Errors.of("repository", "Failed to retrieve advanced suggestions", "REPOSITORY_ERROR")
                    );
                });
    }
}