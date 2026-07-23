package com.portfolio.management.domain.port.incoming;

import io.smallrye.mutiny.Uni;

/**
 * Use case for recording one observed transaction as a usage signal that
 * feeds the popularity ranking (see PopularityPort). Publisher-agnostic:
 * triggered by whatever consumes transaction:created, regardless of which
 * service ends up publishing it.
 */
public interface RecordStockUsageUseCase {

    Uni<Result> execute(Command command);

    record Command(String ticker, String exchange, String currency) {
    }

    sealed interface Result {
        record Success() implements Result {
        }

        record Ignored(String reason) implements Result {
        }

        record Error(String message) implements Result {
        }
    }
}
