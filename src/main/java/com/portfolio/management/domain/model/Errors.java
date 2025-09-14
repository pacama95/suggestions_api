package com.portfolio.management.domain.model;

import java.util.List;

/**
 * Domain model for representing validation and business errors
 */
public record Errors(List<Error> errors) {
    
    public record Error(String field, String message, String code) {
        
        public static Error of(String field, String message) {
            return new Error(field, message, null);
        }
        
        public static Error of(String field, String message, String code) {
            return new Error(field, message, code);
        }
    }
    
    public static Errors of(Error... errors) {
        return new Errors(List.of(errors));
    }
    
    public static Errors of(String field, String message) {
        return new Errors(List.of(Error.of(field, message)));
    }
    
    public static Errors of(String field, String message, String code) {
        return new Errors(List.of(Error.of(field, message, code)));
    }
    
    public boolean isEmpty() {
        return errors == null || errors.isEmpty();
    }
    
    public boolean hasErrors() {
        return !isEmpty();
    }
}
