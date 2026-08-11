package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Strategy for exact ISIN matching. Highest priority: an ISIN uniquely identifies a
 * security, so it outranks every symbol/name heuristic below it.
 */
@ApplicationScoped
public class ExactIsinMatchStrategy implements PriorityStrategy {

    @Override
    public List<Stock> matches(List<Stock> stocks, String query) {
        String queryUpper = query.trim().toUpperCase();
        if (queryUpper.isEmpty()) {
            return List.of();
        }
        return stocks.stream()
                .filter(stock -> stock.isin() != null && stock.isin().toUpperCase().equals(queryUpper))
                .toList();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String description() {
        return "Exact ISIN match";
    }

    @Override
    public SearchField searchField() {
        return SearchField.ISIN;
    }
}
