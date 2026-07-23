package com.portfolio.management.domain.model;

import java.util.List;

/**
 * Filter criteria for fetching stocks from a market data provider.
 * Empty or null lists mean no filtering on that dimension.
 * Filters compose with AND semantics across dimensions; multiple values within
 * a dimension are combined with OR.
 */
public record StockFilter(List<String> countries, List<String> exchanges) {

    public static StockFilter empty() {
        return new StockFilter(List.of(), List.of());
    }

    public boolean isEmpty() {
        return isNullOrEmpty(countries) && isNullOrEmpty(exchanges);
    }

    private static boolean isNullOrEmpty(List<String> values) {
        return values == null || values.isEmpty();
    }
}
