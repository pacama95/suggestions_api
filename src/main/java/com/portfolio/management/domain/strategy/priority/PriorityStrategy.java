package com.portfolio.management.domain.strategy.priority;

import com.portfolio.management.domain.model.Stock;

import java.util.List;

/**
 * Strategy interface for prioritized stock matching
 * Each strategy defines how to match stocks based on different criteria and their priority order
 */
public interface PriorityStrategy {

    /**
     * Find matching stocks based on this strategy's criteria
     *
     * @param stocks The list of stocks to filter
     * @param query  The search query
     * @return List of stocks that match this strategy's criteria
     */
    List<Stock> matches(List<Stock> stocks, String query);

    /**
     * Get the priority of this strategy (lower number = higher priority)
     *
     * @return Priority number where 1 is highest priority
     */
    int priority();

    /**
     * Get a description of what this strategy matches
     *
     * @return Human-readable description
     */
    String description();

    /**
     * Get the stock field this strategy operates on
     *
     * @return The SearchField enum indicating which stock property this strategy matches against
     */
    SearchField searchField();
}
