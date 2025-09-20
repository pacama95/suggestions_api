package com.portfolio.management.infrastructure.adapters.outgoing.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for TwelveData stocks API
 */
public record TwelveDataStockResponse(
    @JsonProperty("data")
    List<TwelveDataStock> data,
    
    @JsonProperty("status")
    String status
) {
    
    /**
     * Individual stock data from TwelveData API
     */
    public record TwelveDataStock(
        @JsonProperty("symbol")
        String symbol,
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("currency")
        String currency,
        
        @JsonProperty("exchange")
        String exchange,
        
        @JsonProperty("mic_code")
        String micCode,
        
        @JsonProperty("country")
        String country,
        
        @JsonProperty("type")
        String type,
        
        @JsonProperty("figi_code")
        String figiCode,
        
        @JsonProperty("cfi_code")
        String cfiCode,
        
        @JsonProperty("isin")
        String isin,
        
        @JsonProperty("cusip")
        String cusip
    ) {
        
        /**
         * Validates if the stock data has required fields
         */
        public boolean isValid() {
            return symbol != null && !symbol.trim().isEmpty() && 
                   name != null && !name.trim().isEmpty();
        }
        
        /**
         * Gets cleaned symbol (trimmed and uppercase)
         */
        public String getCleanedSymbol() {
            return symbol != null ? symbol.trim().toUpperCase() : null;
        }
        
        /**
         * Gets cleaned name (trimmed)
         */
        public String getCleanedName() {
            return name != null ? name.trim() : null;
        }
    }
}
