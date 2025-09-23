package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Strategy for names that contain the query
 */
@ApplicationScoped
public class NameContainsStrategy implements PriorityStrategy {

    @Override
    public List<Stock> matches(List<Stock> stocks, String query) {
        String queryUpper = query.trim().toUpperCase();
        if (queryUpper.isEmpty()) {
            return List.of();
        }
        return stocks.stream()
                .filter(stock -> stock.name().toUpperCase().contains(queryUpper))
                .toList();
    }

    @Override
    public int priority() {
        return 6;
    }

    @Override
    public String description() {
        return "Name contains query";
    }

    @Override
    public SearchField searchField() {
        return SearchField.NAME;
    }
}
