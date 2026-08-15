package com.portfolio.management.domain.model;

/**
 * Domain model representing a stock/ticker with all relevant market data
 */
public record Stock(
        Long id,
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
        Long dataVersion,
        Double popularityScore
) {

    /**
     * Creates a new Stock without ID (for creation scenarios). Popularity is unset (0.0)
     * until the popularity recompute pass runs.
     */
    public static Stock of(String symbol, String name, String currency, String exchange,
                          String micCode, String country, String type, String figiCode,
                          String cfiCode, String isin, String cusip, Long dataVersion) {
        return new Stock(null, symbol, name, currency, exchange, micCode, country, type,
                        figiCode, cfiCode, isin, cusip, dataVersion, 0.0);
    }

    /**
     * Creates a Stock with explicit ID. Popularity is unset (0.0) until the popularity
     * recompute pass runs.
     */
    public static Stock of(Long id, String symbol, String name, String currency, String exchange,
                          String micCode, String country, String type, String figiCode,
                          String cfiCode, String isin, String cusip, Long dataVersion) {
        return new Stock(id, symbol, name, currency, exchange, micCode, country, type,
                        figiCode, cfiCode, isin, cusip, dataVersion, 0.0);
    }
    
    /**
     * Returns display name combining symbol and company name
     */
    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return symbol + " - " + name;
        }
        return symbol;
    }

    /**
     * Returns a copy with {@code type} replaced. Twelve Data's ETF payload omits type,
     * so ingestion defaults it to {@code ETF} without mutating other fields.
     */
    public Stock withType(String type) {
        return new Stock(id, symbol, name, currency, exchange, micCode, country, type,
                figiCode, cfiCode, isin, cusip, dataVersion, popularityScore);
    }
    
    /**
     * Checks if this stock has all required basic information
     */
    public boolean hasBasicInfo() {
        return symbol != null && !symbol.trim().isEmpty() && 
               name != null && !name.trim().isEmpty();
    }
}
