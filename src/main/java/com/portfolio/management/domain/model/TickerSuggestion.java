package com.portfolio.management.domain.model;

public record TickerSuggestion(
    String symbol,
    String name,
    String exchange,
    String type,
    String region,
    String marketCap,
    String currency
) {
    
    public TickerSuggestion {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }

    public TickerSuggestion(String symbol, String name) {
        this(symbol, name, null, null, null, null, null);
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        return symbol.toLowerCase().contains(lowerQuery) || 
               name.toLowerCase().contains(lowerQuery);
    }
}
