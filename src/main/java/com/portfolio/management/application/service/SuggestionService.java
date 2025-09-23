package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.model.Stock;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.domain.strategy.priority.PriorityStrategy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Application service implementing the GetSuggestionsUseCase
 */
@ApplicationScoped
public class SuggestionService implements GetSuggestionsUseCase {

    private static final Logger LOG = Logger.getLogger(SuggestionService.class);

    private static final int FETCH_MULTIPLIER = 5;

    private final StockPort stockPort;
    private final List<PriorityStrategy> priorityStrategies;

    public SuggestionService(StockPort stockPort, Instance<PriorityStrategy> strategyInstances) {
        this.stockPort = stockPort;
        this.priorityStrategies = strategyInstances.stream()
                .sorted(Comparator.comparingInt(PriorityStrategy::priority))
                .toList();
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
        String queryInput = query.input().trim();
        int limit = query.limit();

        return stockPort.findCandidateStocks(queryInput, limit * FETCH_MULTIPLIER)
                .onItem().transform(candidates -> {
                    LOG.infof("Found %d candidate stocks for query: %s", candidates.size(), queryInput);
                    return applyPriorityStrategies(candidates, queryInput, limit);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.errorf(throwable, "Repository error for query: %s", queryInput);
                    return new Result.SystemError(
                            Errors.of("repository", "Failed to retrieve suggestions", "REPOSITORY_ERROR")
                    );
                });
    }

    private Result applyPriorityStrategies(List<Stock> candidates, String query, int totalLimit) {
        LinkedHashSet<Stock> prioritizedResults = new LinkedHashSet<>();

        for (PriorityStrategy strategy : priorityStrategies) {
            if (prioritizedResults.size() >= totalLimit) break;

            List<Stock> matches = strategy.matches(candidates, query);
            int remainingCapacity = totalLimit - prioritizedResults.size();

            matches.stream()
                    .limit(remainingCapacity)
                    .forEach(prioritizedResults::add);

            LOG.debugf("Strategy '%s' (priority %d, field %s) contributed %d matches, total so far: %d",
                    strategy.description(), strategy.priority(), strategy.searchField(),
                    Math.min(matches.size(), remainingCapacity), prioritizedResults.size());
        }

        List<Stock> finalResults = prioritizedResults.stream()
                .limit(totalLimit)
                .toList();

        LOG.infof("Applied %d priority strategies, final results: %d for query: %s",
                priorityStrategies.size(), finalResults.size(), query);
        return new Result.Success(finalResults, query, finalResults.size());
    }
}
