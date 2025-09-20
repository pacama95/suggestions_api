package com.portfolio.management.infrastructure.adapters.incoming.web.mapper;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMapperTest {

    private ErrorMapper errorMapper;

    private Errors.Error domainError;
    private List<Errors.Error> domainErrors;

    @BeforeEach
    void setUp() {
        errorMapper = Mappers.getMapper(ErrorMapper.class);
        
        domainError = new Errors.Error("input", "Search input cannot be empty", "EMPTY_INPUT");
        
        domainErrors = List.of(
            domainError,
            new Errors.Error("limit", "Limit must be between 1 and 100", "INVALID_LIMIT"),
            new Errors.Error("symbol", "Symbol format is invalid", "INVALID_SYMBOL")
        );
    }

    @Test
    @DisplayName("Should successfully map domain Error to ErrorDetail")
    void testToErrorDetail_ValidError_Success() {
        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(domainError);

        // Then
        assertNotNull(result);
        assertEquals(domainError.field(), result.field());
        assertEquals(domainError.message(), result.message());
        assertEquals(domainError.code(), result.code());
    }

    @Test
    @DisplayName("Should handle null domain Error")
    void testToErrorDetail_NullError_ReturnsNull() {
        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should successfully map list of domain Errors to list of ErrorDetails")
    void testToErrorDetailList_ValidErrors_Success() {
        // When
        List<ErrorResponse.ErrorDetail> result = errorMapper.toErrorDetailList(domainErrors);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Check first error
        ErrorResponse.ErrorDetail firstError = result.get(0);
        assertEquals("input", firstError.field());
        assertEquals("Search input cannot be empty", firstError.message());
        assertEquals("EMPTY_INPUT", firstError.code());
        
        // Check second error
        ErrorResponse.ErrorDetail secondError = result.get(1);
        assertEquals("limit", secondError.field());
        assertEquals("Limit must be between 1 and 100", secondError.message());
        assertEquals("INVALID_LIMIT", secondError.code());
        
        // Check third error
        ErrorResponse.ErrorDetail thirdError = result.get(2);
        assertEquals("symbol", thirdError.field());
        assertEquals("Symbol format is invalid", thirdError.message());
        assertEquals("INVALID_SYMBOL", thirdError.code());
    }

    @Test
    @DisplayName("Should handle empty list of domain Errors")
    void testToErrorDetailList_EmptyList_ReturnsEmptyList() {
        // Given
        List<Errors.Error> emptyList = List.of();

        // When
        List<ErrorResponse.ErrorDetail> result = errorMapper.toErrorDetailList(emptyList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle null list of domain Errors")
    void testToErrorDetailList_NullList_ReturnsNull() {
        // When
        List<ErrorResponse.ErrorDetail> result = errorMapper.toErrorDetailList(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle Error with null code")
    void testToErrorDetail_NullCode_Success() {
        // Given
        Errors.Error errorWithNullCode = new Errors.Error("field", "Some error message", null);

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithNullCode);

        // Then
        assertNotNull(result);
        assertEquals("field", result.field());
        assertEquals("Some error message", result.message());
        assertNull(result.code());
    }

    @Test
    @DisplayName("Should handle Error with null field")
    void testToErrorDetail_NullField_Success() {
        // Given
        Errors.Error errorWithNullField = new Errors.Error(null, "Some error message", "ERROR_CODE");

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithNullField);

        // Then
        assertNotNull(result);
        assertNull(result.field());
        assertEquals("Some error message", result.message());
        assertEquals("ERROR_CODE", result.code());
    }

    @Test
    @DisplayName("Should handle Error with null message")
    void testToErrorDetail_NullMessage_Success() {
        // Given
        Errors.Error errorWithNullMessage = new Errors.Error("field", null, "ERROR_CODE");

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithNullMessage);

        // Then
        assertNotNull(result);
        assertEquals("field", result.field());
        assertNull(result.message());
        assertEquals("ERROR_CODE", result.code());
    }

    @Test
    @DisplayName("Should handle Error with empty strings")
    void testToErrorDetail_EmptyStrings_Success() {
        // Given
        Errors.Error errorWithEmptyStrings = new Errors.Error("", "", "");

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithEmptyStrings);

        // Then
        assertNotNull(result);
        assertEquals("", result.field());
        assertEquals("", result.message());
        assertEquals("", result.code());
    }

    @Test
    @DisplayName("Should preserve special characters in error fields")
    void testToErrorDetail_SpecialCharacters_Success() {
        // Given
        Errors.Error errorWithSpecialChars = new Errors.Error(
            "user.email",
            "Email format is invalid: missing '@' character",
            "VALIDATION_EMAIL_FORMAT"
        );

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithSpecialChars);

        // Then
        assertNotNull(result);
        assertEquals("user.email", result.field());
        assertEquals("Email format is invalid: missing '@' character", result.message());
        assertEquals("VALIDATION_EMAIL_FORMAT", result.code());
    }

    @Test
    @DisplayName("Should handle long error messages")
    void testToErrorDetail_LongMessage_Success() {
        // Given
        String longMessage = "This is a very long error message that contains a lot of details about what went wrong during the validation process and provides comprehensive information to help users understand the issue";
        Errors.Error errorWithLongMessage = new Errors.Error("field", longMessage, "LONG_ERROR");

        // When
        ErrorResponse.ErrorDetail result = errorMapper.toErrorDetail(errorWithLongMessage);

        // Then
        assertNotNull(result);
        assertEquals("field", result.field());
        assertEquals(longMessage, result.message());
        assertEquals("LONG_ERROR", result.code());
    }
}
