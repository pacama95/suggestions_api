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
        Long dataVersion
) {
    
    /**
     * Creates a new Stock without ID (for creation scenarios)
     */
    public static Stock of(String symbol, String name, String currency, String exchange, 
                          String micCode, String country, String type, String figiCode, 
                          String cfiCode, String isin, String cusip, Long dataVersion) {
        return new Stock(null, symbol, name, currency, exchange, micCode, country, type, 
                        figiCode, cfiCode, isin, cusip, dataVersion);
    }
    
    /**
     * Creates a Stock with explicit ID
     */
    public static Stock of(Long id, String symbol, String name, String currency, String exchange, 
                          String micCode, String country, String type, String figiCode, 
                          String cfiCode, String isin, String cusip, Long dataVersion) {
        return new Stock(id, symbol, name, currency, exchange, micCode, country, type, 
                        figiCode, cfiCode, isin, cusip, dataVersion);
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
     * Checks if this stock has all required basic information
     */
    public boolean hasBasicInfo() {
        return symbol != null && !symbol.trim().isEmpty() && 
               name != null && !name.trim().isEmpty();
    }
}
