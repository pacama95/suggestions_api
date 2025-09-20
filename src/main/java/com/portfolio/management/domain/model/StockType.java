package com.portfolio.management.domain.model;

/**
 * Domain model representing a stock type category
 */
public record StockType(
    Long id,
    String code,
    String name,
    String description,
    boolean isActive
) {
    
    /**
     * Creates a new StockType with default active state
     */
    public static StockType of(String code, String name, String description) {
        return new StockType(null, code, name, description, true);
    }
    
    /**
     * Creates a StockType with explicit active state
     */
    public static StockType of(Long id, String code, String name, String description, boolean isActive) {
        return new StockType(id, code, name, description, isActive);
    }
    
    /**
     * Returns display name for UI
     */
    public String getDisplayName() {
        return name != null ? name : code;
    }
    
    /**
     * Checks if this is a common stock type
     */
    public boolean isCommonStock() {
        return "COMMON_STOCK".equals(code);
    }
    
    /**
     * Checks if this is an ETF type
     */
    public boolean isETF() {
        return "ETF".equals(code);
    }
    
    /**
     * Checks if this is a REIT type
     */
    public boolean isREIT() {
        return "REIT".equals(code);
    }
    
    /**
     * Checks if this is an ADR type
     */
    public boolean isADR() {
        return "ADR".equals(code);
    }
}
