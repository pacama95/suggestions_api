package com.portfolio.management.infrastructure.adapters.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for ticker suggestions
 */
@RegisterForReflection
public record SuggestionsRequest(
    
    @NotBlank(message = "Query parameter cannot be empty")
    String q,
    
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 50, message = "Limit cannot exceed 50")
    Integer limit
) {
    
    public SuggestionsRequest {
        // Set default limit if not provided
        if (limit == null) {
            limit = 10;
        }
    }
    
    public SuggestionsRequest(String q) {
        this(q, 10);
    }
}
