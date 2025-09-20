package com.portfolio.management.infrastructure.adapters.incoming.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for stock type operations
 */
@RegisterForReflection
@Schema(
    name = "StockTypeResponse",
    description = "Response containing stock type information"
)
public record StockTypeResponse(
    @Schema(
        description = "List of stock types",
        example = "[{\"id\":1,\"code\":\"CS\",\"name\":\"Common Stock\",\"description\":\"Ordinary shares that represent ownership in a company\",\"active\":true}]"
    )
    List<StockTypeDto> stockTypes,
    
    @Schema(
        description = "Total number of stock types returned",
        example = "1",
        minimum = "0"
    )
    long count
) {
    
    /**
     * Constructor for single stock type response
     */
    public StockTypeResponse(StockTypeDto stockType) {
        this(List.of(stockType), 1);
    }
    
    @RegisterForReflection
    @Schema(
        name = "StockTypeDto",
        description = "Individual stock type information"
    )
    public record StockTypeDto(
        @Schema(
            description = "Stock type unique identifier",
            example = "1",
            required = true
        )
        Long id,
        
        @Schema(
            description = "Stock type code",
            example = "CS",
            required = true
        )
        String code,
        
        @Schema(
            description = "Stock type name",
            example = "Common Stock",
            required = true
        )
        String name,
        
        @Schema(
            description = "Stock type description",
            example = "Ordinary shares that represent ownership in a company"
        )
        String description,
        
        @Schema(
            description = "Whether the stock type is active",
            example = "true",
            required = true
        )
        boolean active
    ) {
    }
}
