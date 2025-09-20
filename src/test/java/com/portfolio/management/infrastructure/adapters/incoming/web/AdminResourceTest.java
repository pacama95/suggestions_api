package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminResourceTest {

    @Mock
    FetchAndStoreStockDataUseCase fetchAndStoreStockDataUseCase;

    private AdminResource adminResource;

    @BeforeEach
    void setUp() {
        adminResource = new AdminResource(fetchAndStoreStockDataUseCase);
    }

    @Test
    @DisplayName("Should successfully fetch stocks and return success response")
    void testFetchStocks_Success() {
        // Given
        FetchAndStoreStockDataUseCase.Result successResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 1000, "Successfully fetched and stored 1000 stocks"
        );
        
        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks())
            .thenReturn(Uni.createFrom().item(successResult));

        // When
        Uni<Response> response = adminResource.fetchStocks();
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(true, responseEntity.get("success"));
        assertEquals("Successfully fetched and stored 1000 stocks", responseEntity.get("message"));
        assertEquals(1000, responseEntity.get("recordsProcessed"));
        
        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks();
    }

    @Test
    @DisplayName("Should return success with 0 processed items response when stock fetch fails")
    void testFetchStocks_Failure() {
        // Given
        FetchAndStoreStockDataUseCase.Result failureResult = new FetchAndStoreStockDataUseCase.Result.Success(
            false, 0, "Failed to connect to TwelveData API"
        );
        
        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks())
            .thenReturn(Uni.createFrom().item(failureResult));

        // When
        Uni<Response> response = adminResource.fetchStocks();
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(false, responseEntity.get("success"));
        assertEquals("Failed to connect to TwelveData API", responseEntity.get("message"));
        assertEquals(0, responseEntity.get("recordsProcessed"));
        
        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks();
    }

    @Test
    @DisplayName("Should handle service exception during stock fetch")
    void testFetchStocks_ServiceException() {
        // Given
        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks())
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Service unavailable")));

        // When
        Uni<Response> response = adminResource.fetchStocks();
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatus());
        
        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks();
    }

    @Test
    @DisplayName("Should handle partial success with some records processed")
    void testFetchStocks_PartialSuccess() {
        // Given
        FetchAndStoreStockDataUseCase.Result partialResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 500, "Processed 500 out of 1000 stocks successfully"
        );
        
        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks())
            .thenReturn(Uni.createFrom().item(partialResult));

        // When
        Uni<Response> response = adminResource.fetchStocks();
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Then
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(true, responseEntity.get("success"));
        assertEquals("Processed 500 out of 1000 stocks successfully", responseEntity.get("message"));
        assertEquals(500, responseEntity.get("recordsProcessed"));
        
        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks();
    }
}
