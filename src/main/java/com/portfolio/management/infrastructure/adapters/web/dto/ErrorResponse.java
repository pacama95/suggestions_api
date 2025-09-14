package com.portfolio.management.infrastructure.adapters.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Error response DTO
 */
@RegisterForReflection
@Schema(
    name = "ErrorResponse",
    description = "Standard error response format for API errors including validation and system errors"
)
public record ErrorResponse(
    @Schema(
        description = "List of specific error details, empty for general errors",
        example = "[{\"field\":\"input\",\"message\":\"Search input cannot be empty\",\"code\":\"EMPTY_INPUT\"}]"
    )
    List<ErrorDetail> errors,
    
    @Schema(
        description = "General error message describing what went wrong",
        example = "Validation failed"
    )
    String message,
    
    @Schema(
        description = "Unix timestamp (milliseconds) when the error occurred",
        example = "1694771234567"
    )
    long timestamp
) {
    
    @RegisterForReflection
    @Schema(
        name = "ErrorDetail",
        description = "Detailed information about a specific validation or field error"
    )
    public record ErrorDetail(
        @Schema(
            description = "The field or parameter that caused the error",
            example = "input"
        )
        String field,
        
        @Schema(
            description = "Human-readable error message",
            example = "Search input cannot be empty"
        )
        String message,
        
        @Schema(
            description = "Machine-readable error code for programmatic handling",
            example = "EMPTY_INPUT"
        )
        String code
    ) {
    }
    
    public static ErrorResponse of(String message) {
        return new ErrorResponse(List.of(), message, Instant.now().toEpochMilli());
    }
    
    public static ErrorResponse of(List<ErrorDetail> errors, String message) {
        return new ErrorResponse(errors, message, Instant.now().toEpochMilli());
    }
}
