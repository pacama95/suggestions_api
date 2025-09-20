package com.portfolio.management.infrastructure.adapters.incoming.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for exchange operations
 */
@RegisterForReflection
@Schema(
    name = "ExchangeResponse",
    description = "Response containing exchange information"
)
public record ExchangeResponse(
    @Schema(
        description = "List of exchanges",
        example = "[{\"id\":1,\"code\":\"NYSE\",\"name\":\"New York Stock Exchange\",\"country\":\"United States\",\"timezone\":\"America/New_York\",\"currencyCode\":\"USD\",\"active\":true}]"
    )
    List<ExchangeDto> exchanges,
    
    @Schema(
        description = "Total number of exchanges returned",
        example = "1",
        minimum = "0"
    )
    long count
) {
    
    /**
     * Constructor for single exchange response
     */
    public ExchangeResponse(ExchangeDto exchange) {
        this(List.of(exchange), 1);
    }
    
    @RegisterForReflection
    @Schema(
        name = "ExchangeDto",
        description = "Individual exchange information"
    )
    public record ExchangeDto(
        @Schema(
            description = "Exchange unique identifier",
            example = "1",
            required = true
        )
        Long id,
        
        @Schema(
            description = "Exchange code",
            example = "NYSE",
            required = true
        )
        String code,
        
        @Schema(
            description = "Exchange full name",
            example = "New York Stock Exchange",
            required = true
        )
        String name,
        
        @Schema(
            description = "Country where the exchange is located",
            example = "United States"
        )
        String country,
        
        @Schema(
            description = "Exchange timezone",
            example = "America/New_York"
        )
        String timezone,
        
        @Schema(
            description = "Primary trading currency code",
            example = "USD"
        )
        String currencyCode,
        
        @Schema(
            description = "Whether the exchange is active",
            example = "true",
            required = true
        )
        boolean active
    ) {
    }
}
