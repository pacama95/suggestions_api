package com.portfolio.management.infrastructure.adapters.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.List;

/**
 * Error response DTO
 */
@RegisterForReflection
public record ErrorResponse(
    List<ErrorDetail> errors,
    String message,
    long timestamp
) {
    
    @RegisterForReflection
    public record ErrorDetail(
        String field,
        String message,
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
