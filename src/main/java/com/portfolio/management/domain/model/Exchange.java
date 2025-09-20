package com.portfolio.management.domain.model;

/**
 * Domain model representing a stock exchange
 */
public record Exchange(
        Long id,
        String code,
        String name,
        String country,
        String timezone,
        String currencyCode,
        boolean isActive
) {

    /**
     * Creates a new Exchange with default active state
     */
    public static Exchange of(String code, String name, String country, String currencyCode) {
        return new Exchange(null, code, name, country, null, currencyCode, true);
    }

    /**
     * Creates an Exchange with timezone
     */
    public static Exchange of(String code, String name, String country, String timezone, String currencyCode) {
        return new Exchange(null, code, name, country, timezone, currencyCode, true);
    }

    /**
     * Creates an Exchange with explicit active state
     */
    public static Exchange of(Long id, String code, String name, String country, String timezone, String currencyCode, boolean isActive) {
        return new Exchange(id, code, name, country, timezone, currencyCode, isActive);
    }

    /**
     * Returns display name for UI
     */
    public String getDisplayName() {
        if (name != null && country != null) {
            return name + " (" + country + ")";
        }
        return name != null ? name : code;
    }

    /**
     * Returns short display name (code)
     */
    public String getShortName() {
        return code;
    }

    /**
     * Checks if this is a US exchange
     */
    public boolean isUSExchange() {
        return "United States".equals(country) ||
                "NYSE".equals(code) || "NASDAQ".equals(code) || "OTC".equals(code);
    }

    /**
     * Checks if this is a European exchange
     */
    public boolean isEuropeanExchange() {
        return country != null && (
                country.contains("Germany") || country.contains("United Kingdom") ||
                        country.contains("Italy") || country.contains("Austria") ||
                        "Europe".equals(country)
        );
    }

    /**
     * Checks if this is an Asian exchange
     */
    public boolean isAsianExchange() {
        return country != null && (
                country.contains("Japan") || country.contains("China") ||
                        country.contains("Hong Kong") || country.contains("Korea") ||
                        country.contains("Taiwan") || country.contains("India") ||
                        country.contains("Thailand")
        );
    }

    /**
     * Gets country based on country
     */
    public String getRegion() {
        if (isUSExchange()) return "North America";
        if (isEuropeanExchange()) return "Europe";
        if (isAsianExchange()) return "Asia";
        if (country != null && (country.contains("Canada"))) return "North America";
        if (country != null && (country.contains("Australia"))) return "Oceania";
        if (country != null && (country.contains("Brazil"))) return "South America";
        return "Other";
    }
}
