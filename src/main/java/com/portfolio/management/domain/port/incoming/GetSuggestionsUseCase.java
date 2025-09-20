package com.portfolio.management.domain.port.incoming;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.model.Stock;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Use case for getting ticker suggestions based on user input
 */
public interface GetSuggestionsUseCase {

    Uni<Result> execute(Query query);


    record Query(String input, int limit) {

        public Query {
            if (input == null) {
                throw new IllegalArgumentException("Input cannot be null");
            }
            if (limit <= 0 || limit > 50) {
                throw new IllegalArgumentException("Limit must be between 1 and 50");
            }
        }

        public Query(String input) {
            this(input, 10); // Default limit
        }
    }

    sealed interface Result {

        record Success(List<Stock> suggestions, String query, int count) implements Result {

            public Success {
                if (suggestions == null) {
                    throw new IllegalArgumentException("Suggestions cannot be null");
                }
                count = suggestions.size();
            }
        }

        record ValidationError(Errors errors) implements Result {
        }

        record SystemError(Errors errors) implements Result {
        }
    }
}
