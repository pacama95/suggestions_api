package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.Currency;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for currency operations
 */
public interface CurrencyRepository {
    
    /**
     * Find all active currencies
     * @return List of all active currencies
     */
    Uni<List<Currency>> findAll();
    
    /**
     * Search currencies by query string (matches code, name, or country code)
     * @param query Search query
     * @return List of matching currencies
     */
    Uni<List<Currency>> search(String query);
    
    /**
     * Find currency by code
     * @param code Currency code (e.g., USD, EUR)
     * @return Optional currency if found
     */
    Uni<Optional<Currency>> findByCode(String code);
    
    /**
     * Find currency by ID
     * @param id Currency ID
     * @return Optional currency if found
     */
    Uni<Optional<Currency>> findById(Long id);
    
    /**
     * Find currencies by country code
     * @param countryCode Country code
     * @return List of currencies for the country
     */
    Uni<List<Currency>> findByCountryCode(String countryCode);
    
    /**
     * Find major currencies (USD, EUR, JPY, GBP, etc.)
     * @return List of major currencies
     */
    Uni<List<Currency>> findMajorCurrencies();
    
    /**
     * Count total active currencies
     * @return Total count of active currencies
     */
    Uni<Long> count();
}
