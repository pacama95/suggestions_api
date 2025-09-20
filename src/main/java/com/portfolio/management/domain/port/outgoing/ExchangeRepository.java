package com.portfolio.management.domain.port.outgoing;

import com.portfolio.management.domain.model.Exchange;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for exchange operations
 */
public interface ExchangeRepository {
    
    /**
     * Find all active exchanges
     * @return List of all active exchanges
     */
    Uni<List<Exchange>> findAll();
    
    /**
     * Search exchanges by query string (matches code, name, or country)
     * @param query Search query
     * @return List of matching exchanges
     */
    Uni<List<Exchange>> search(String query);
    
    /**
     * Find exchange by code
     * @param code Exchange code (e.g., NYSE, NASDAQ)
     * @return Optional exchange if found
     */
    Uni<Optional<Exchange>> findByCode(String code);
    
    /**
     * Find exchange by ID
     * @param id Exchange ID
     * @return Optional exchange if found
     */
    Uni<Optional<Exchange>> findById(Long id);
    
    /**
     * Find exchanges by country
     * @param country Country name
     * @return List of exchanges in the country
     */
    Uni<List<Exchange>> findByCountry(String country);
    
    /**
     * Find exchanges by currency code
     * @param currencyCode Currency code
     * @return List of exchanges trading in the currency
     */
    Uni<List<Exchange>> findByCurrencyCode(String currencyCode);
    
    /**
     * Find US exchanges
     * @return List of US exchanges
     */
    Uni<List<Exchange>> findUSExchanges();
    
    /**
     * Find European exchanges
     * @return List of European exchanges
     */
    Uni<List<Exchange>> findEuropeanExchanges();
    
    /**
     * Find Asian exchanges
     * @return List of Asian exchanges
     */
    Uni<List<Exchange>> findAsianExchanges();
    
    /**
     * Count total active exchanges
     * @return Total count of active exchanges
     */
    Uni<Long> count();
}
