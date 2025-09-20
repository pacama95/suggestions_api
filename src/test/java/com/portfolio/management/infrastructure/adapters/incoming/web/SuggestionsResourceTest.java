package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetSuggestionsAdvancedUseCase;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.StockMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ErrorResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionsResourceTest {

    @Mock
    GetSuggestionsUseCase mockGetSuggestionsUseCase;

    @Mock
    GetSuggestionsAdvancedUseCase mockGetSuggestionsAdvancedUseCase;

    @Mock
    StockMapper mockSuggestionMapper;

    @Mock
    ErrorMapper mockErrorMapper;

    private SuggestionsResource suggestionsResource;

    private List<ErrorResponse.ErrorDetail> mockErrorDetails;

    @BeforeEach
    void setUp() {
        suggestionsResource = new SuggestionsResource(
            mockGetSuggestionsUseCase, 
            mockGetSuggestionsAdvancedUseCase, 
            mockSuggestionMapper, 
            mockErrorMapper
        );
    }

    @Test
    @DisplayName("Should return bad request for advanced search validation error")
    void testAdvancedSearch_ValidationError_BadRequest() {
        // Given
        Errors errors = Errors.of("limit", "Limit must be positive", "INVALID_LIMIT");
        GetSuggestionsAdvancedUseCase.Result.ValidationError validationError = 
            new GetSuggestionsAdvancedUseCase.Result.ValidationError(errors);
        
        when(mockGetSuggestionsAdvancedUseCase.execute(any(GetSuggestionsAdvancedUseCase.Query.class)))
            .thenReturn(Uni.createFrom().item(validationError));
        when(mockErrorMapper.toErrorDetailList(errors.errors()))
            .thenReturn(mockErrorDetails);

        // When
        Uni<Response> response = suggestionsResource.advancedSearch("AAPL", null, null, null, null, 10);
        Response result = response.await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) result.getEntity();
        assertNotNull(errorResponse);
        assertEquals("Validation failed", errorResponse.message());
        
        verify(mockGetSuggestionsAdvancedUseCase, times(1)).execute(any(GetSuggestionsAdvancedUseCase.Query.class));
        verify(mockErrorMapper, times(1)).toErrorDetailList(errors.errors());
    }

    @Test
    @DisplayName("Should handle advanced search use case failure")
    void testAdvancedSearch_UseCaseFailure_InternalServerError() {
        // Given
        when(mockGetSuggestionsAdvancedUseCase.execute(any(GetSuggestionsAdvancedUseCase.Query.class)))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Service unavailable")));

        // When
        Uni<Response> response = suggestionsResource.advancedSearch("AAPL", null, null, null, null, 10);
        Response result = response.await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) result.getEntity();
        assertNotNull(errorResponse);
        assertEquals("An unexpected error occurred", errorResponse.message());
    }
}