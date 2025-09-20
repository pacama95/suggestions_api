package com.portfolio.management.infrastructure.adapters.incoming.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for currency operations
 */
@RegisterForReflection
@Schema(
    name = "CurrencyResponse",
    description = "Response containing currency information"
)
public record CurrencyResponse(
    @Schema(
        description = "List of currencies",
        example = "[{\"id\":1,\"code\":\"USD\",\"name\":\"United States Dollar\",\"symbol\":\"$\",\"countryCode\":\"US\",\"active\":true}]"
    )
    List<CurrencyDto> currencies,
    
    @Schema(
        description = "Total number of currencies returned",
        example = "1",
        minimum = "0"
    )
    long count
) {
    
    /**
     * Constructor for single currency response
     */
    public CurrencyResponse(CurrencyDto currency) {
        this(List.of(currency), 1);
    }
    
    @RegisterForReflection
    @Schema(
        name = "CurrencyDto",
        description = "Individual currency information"
    )
    public record CurrencyDto(
        @Schema(
            description = "Currency unique identifier",
            example = "1",
            required = true
        )
        Long id,
        
        @Schema(
            description = "Currency code (ISO 4217)",
            example = "USD",
            required = true
        )
        String code,
        
        @Schema(
            description = "Currency full name",
            example = "United States Dollar",
            required = true
        )
        String name,
        
        @Schema(
            description = "Currency symbol",
            example = "$"
        )
        String symbol,
        
        @Schema(
            description = "Country code (ISO 3166-1 alpha-2)",
            example = "US"
        )
        String countryCode,
        
        @Schema(
            description = "Whether the currency is active",
            example = "true",
            required = true
        )
        boolean active
    ) {
    }
}
