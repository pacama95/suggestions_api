package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Strategy for exact symbol matching (highest priority)
 */
@ApplicationScoped
public class ExactSymbolMatchStrategy implements PriorityStrategy {

    @Override
    public List<Stock> matches(List<Stock> stocks, String query) {
        String queryUpper = query.trim().toUpperCase();
        if (queryUpper.isEmpty()) {
            return List.of();
        }
        return stocks.stream()
                .filter(stock -> stock.symbol().toUpperCase().equals(queryUpper))
                .toList();
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public String description() {
        return "Exact symbol match";
    }

    @Override
    public SearchField searchField() {
        return SearchField.SYMBOL;
    }
}
