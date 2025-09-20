package com.portfolio.management.domain.model;

/**
 * Domain model representing a currency
 */
public record Currency(
    Long id,
    String code,
    String name,
    String symbol,
    String countryCode,
    boolean isActive
) {
    
    /**
     * Creates a new Currency with default active state
     */
    public static Currency of(String code, String name, String symbol, String countryCode) {
        return new Currency(null, code, name, symbol, countryCode, true);
    }
    
    /**
     * Creates a Currency with explicit active state
     */
    public static Currency of(Long id, String code, String name, String symbol, String countryCode, boolean isActive) {
        return new Currency(id, code, name, symbol, countryCode, isActive);
    }
    
    /**
     * Returns display name for UI (symbol + code)
     */
    public String getDisplayName() {
        if (symbol != null && !symbol.isEmpty()) {
            return symbol + " " + code;
        }
        return code;
    }
    
    /**
     * Returns full description (name + code)
     */
    public String getFullDescription() {
        return name + " (" + code + ")";
    }
    
    /**
     * Checks if this is USD
     */
    public boolean isUSD() {
        return "USD".equals(code);
    }
    
    /**
     * Checks if this is EUR
     */
    public boolean isEUR() {
        return "EUR".equals(code);
    }
    
    /**
     * Checks if this is a major currency (USD, EUR, JPY, GBP, CHF, CAD, AUD)
     */
    public boolean isMajorCurrency() {
        return code != null && (
            code.equals("USD") || code.equals("EUR") || code.equals("JPY") || 
            code.equals("GBP") || code.equals("CHF") || code.equals("CAD") || 
            code.equals("AUD")
        );
    }
}
