package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Strategy for symbols that contain the query
 */
@ApplicationScoped
public class SymbolContainsStrategy implements PriorityStrategy {

    @Override
    public List<Stock> matches(List<Stock> stocks, String query) {
        String queryUpper = query.trim().toUpperCase();
        if (queryUpper.isEmpty()) {
            return List.of();
        }
        return stocks.stream()
                .filter(stock -> stock.symbol().toUpperCase().contains(queryUpper))
                .toList();
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String description() {
        return "Symbol contains query";
    }

    @Override
    public SearchField searchField() {
        return SearchField.SYMBOL;
    }
}
