package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.domain.strategy.priority.PriorityStrategy;
import com.portfolio.management.domain.strategy.priority.util.QueryFieldExtractor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Application service implementing the GetSuggestionsAdvancedUseCase
 */
@ApplicationScoped
public class AdvancedSuggestionService implements GetSuggestionsAdvancedUseCase {

    private static final Logger LOG = Logger.getLogger(AdvancedSuggestionService.class);

    private static final int FETCH_MULTIPLIER = 3;

    private final StockPort stockPort;
    private final List<PriorityStrategy> priorityStrategies;

    public AdvancedSuggestionService(StockPort stockPort, Instance<PriorityStrategy> strategyInstances) {
        this.stockPort = stockPort;
        this.priorityStrategies = strategyInstances.stream()
                .sorted(Comparator.comparingInt(PriorityStrategy::priority))
                .toList();
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
        String searchDescription = query.getSearchDescription();
        int limit = query.limit();

        return stockPort.findByAdvancedSearch(
                        query.symbol(),
                        query.companyName(),
                        query.exchange(),
                        query.country(),
                        query.currency(),
                        limit * FETCH_MULTIPLIER)
                .onItem().transform(candidates -> {
                    LOG.infof("Found %d candidate stocks for advanced search: %s", candidates.size(), searchDescription);
                    return applyPriorityStrategies(candidates, query, searchDescription, limit);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Repository error for advanced search: %s", searchDescription);
                    return new Result.SystemError(
                            Errors.of("repository", "Failed to retrieve advanced suggestions", "REPOSITORY_ERROR")
                    );
                });
    }

    private Result applyPriorityStrategies(List<Stock> candidates, Query originalQuery, String searchDescription, int totalLimit) {
        LinkedHashSet<Stock> prioritizedResults = new LinkedHashSet<>();

        for (PriorityStrategy strategy : priorityStrategies) {
            if (prioritizedResults.size() >= totalLimit) break;

            String searchTerm = QueryFieldExtractor.extractSearchTerm(originalQuery, strategy.searchField());

            // Skip this strategy if the query doesn't have a value for its field
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                LOG.debugf("Skipping strategy '%s' - no search term provided for field %s",
                        strategy.description(), strategy.searchField());
                continue;
            }

            List<Stock> matches = strategy.matches(candidates, searchTerm);
            int remainingCapacity = totalLimit - prioritizedResults.size();

            matches.stream()
                    .limit(remainingCapacity)
                    .forEach(prioritizedResults::add);

            LOG.debugf("Strategy '%s' (priority %d, field %s) contributed %d matches for advanced search, total so far: %d",
                    strategy.description(), strategy.priority(), strategy.searchField(),
                    Math.min(matches.size(), remainingCapacity), prioritizedResults.size());
        }

        List<Stock> finalResults = prioritizedResults.stream()
                .limit(totalLimit)
                .toList();

        LOG.infof("Applied %d priority strategies to advanced search, final results: %d for: %s",
                priorityStrategies.size(), finalResults.size(), searchDescription);
        return new Result.Success(finalResults, searchDescription, finalResults.size());
    }

}