package com.portfolio.management.application.service;

import com.portfolio.management.domain.port.incoming.RecordStockUsageUseCase;
import com.portfolio.management.domain.port.outgoing.PopularityPort;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecordStockUsageService implements RecordStockUsageUseCase {

    private final PopularityPort popularityPort;

    public RecordStockUsageService(PopularityPort popularityPort) {
        this.popularityPort = popularityPort;
    }

    @Override
    public Uni<Result> execute(Command command) {
        if (command.ticker() == null || command.ticker().isBlank()) {
            return Uni.createFrom().item(new Result.Ignored("Missing ticker"));
        }

        String symbol = command.ticker().trim().toUpperCase();

        return popularityPort.incrementUsage(symbol, command.exchange(), command.currency())
                .onItem().transform(ignored -> (Result) new Result.Success())
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf(throwable, "Failed to record stock usage for ticker: %s", symbol);
                    return new Result.Error("Failed to record stock usage: " + throwable.getMessage());
                });
    }
}
