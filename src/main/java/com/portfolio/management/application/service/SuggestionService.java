package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.domain.port.outgoing.SuggestionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Application service implementing the GetSuggestionsUseCase
 */
@ApplicationScoped
public class SuggestionService implements GetSuggestionsUseCase {
    
    private static final Logger LOG = Logger.getLogger(SuggestionService.class);
    
    private final SuggestionRepository repository;
    
    @Inject
    public SuggestionService(SuggestionRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Uni<Result> execute(Query query) {
        LOG.debugf("Executing suggestion search for query: %s, limit: %d", query.input(), query.limit());
        
        return validateQuery(query)
            .onItem().transformToUni(validationResult -> {
                if (validationResult != null) {
                    return Uni.createFrom().item(validationResult);
                }
                
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
            
            if (query.input().trim().length() < 1) {
                return new Result.ValidationError(
                    Errors.of("input", "Search input must be at least 1 character", "INPUT_TOO_SHORT")
                );
            }
            
            // No validation errors
            return null;
        });
    }
    
    private Uni<Result> searchSuggestions(Query query) {
        return repository.findByQuery(query.input().trim(), query.limit())
            .onItem().transform(suggestions -> {
                LOG.debugf("Found %d suggestions for query: %s", suggestions.size(), query.input());
                return (Result) new Result.Success(suggestions, query.input(), suggestions.size());
            })
            .onFailure().recoverWithItem(throwable -> {
                LOG.errorf(throwable, "Repository error for query: %s", query.input());
                return (Result) new Result.SystemError(
                    Errors.of("repository", "Failed to retrieve suggestions", "REPOSITORY_ERROR")
                );
            });
    }
}
