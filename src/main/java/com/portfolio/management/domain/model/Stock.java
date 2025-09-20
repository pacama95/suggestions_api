package com.portfolio.management.domain.model;

public record Stock(
        String symbol,
        String name,
        String currency,
        String exchange,
        String micCode,
        String country,
        String type,
        String figiCode,
        String cfiCode,
        String isin,
        String cusip,
        Long dataVersion
) {
}
