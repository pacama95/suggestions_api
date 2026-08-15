package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.model.StockFilter;
import com.portfolio.management.domain.port.incoming.FetchAndStoreStockDataUseCase;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        FetchAndStoreStockDataUseCase.Result successResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 1000, "Successfully fetched and stored 1000 stocks"
        );

        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().item(successResult));

        Uni<Response> response = adminResource.fetchStocks(null, null);
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(true, responseEntity.get("success"));
        assertEquals("Successfully fetched and stored 1000 stocks", responseEntity.get("message"));
        assertEquals(1000, responseEntity.get("recordsProcessed"));

        ArgumentCaptor<StockFilter> filterCaptor = ArgumentCaptor.forClass(StockFilter.class);
        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks(filterCaptor.capture());
        assertTrue(filterCaptor.getValue().isEmpty());
    }

    @Test
    @DisplayName("Should pass country and exchange filters from request")
    void testFetchStocks_WithFilters() {
        FetchAndStoreStockDataUseCase.Result successResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 100, "Successfully fetched and stored stocks"
        );

        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().item(successResult));

        adminResource.fetchStocks(List.of("United States"), List.of("NASDAQ", "NYSE"))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        ArgumentCaptor<StockFilter> filterCaptor = ArgumentCaptor.forClass(StockFilter.class);
        verify(fetchAndStoreStockDataUseCase).fetchAndStoreStocks(filterCaptor.capture());
        assertEquals(List.of("United States"), filterCaptor.getValue().countries());
        assertEquals(List.of("NASDAQ", "NYSE"), filterCaptor.getValue().exchanges());
    }

    @Test
    @DisplayName("Should return success with 0 processed items response when stock fetch fails")
    void testFetchStocks_Failure() {
        FetchAndStoreStockDataUseCase.Result failureResult = new FetchAndStoreStockDataUseCase.Result.Success(
            false, 0, "Failed to connect to TwelveData API"
        );

        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().item(failureResult));

        Uni<Response> response = adminResource.fetchStocks(null, null);
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(false, responseEntity.get("success"));
        assertEquals("Failed to connect to TwelveData API", responseEntity.get("message"));
        assertEquals(0, responseEntity.get("recordsProcessed"));

        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks(any(StockFilter.class));
    }

    @Test
    @DisplayName("Should handle service exception during stock fetch")
    void testFetchStocks_ServiceException() {
        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Service unavailable")));

        Uni<Response> response = adminResource.fetchStocks(null, null);
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatus());

        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks(any(StockFilter.class));
    }

    @Test
    @DisplayName("Should handle partial success with some records processed")
    void testFetchStocks_PartialSuccess() {
        FetchAndStoreStockDataUseCase.Result partialResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 500, "Processed 500 out of 1000 stocks successfully"
        );

        when(fetchAndStoreStockDataUseCase.fetchAndStoreStocks(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().item(partialResult));

        Uni<Response> response = adminResource.fetchStocks(null, null);
        Response result = response
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertNotNull(responseEntity);
        assertEquals(true, responseEntity.get("success"));
        assertEquals("Processed 500 out of 1000 stocks successfully", responseEntity.get("message"));
        assertEquals(500, responseEntity.get("recordsProcessed"));

        verify(fetchAndStoreStockDataUseCase, times(1)).fetchAndStoreStocks(any(StockFilter.class));
    }

    @Test
    @DisplayName("Should append ETFs without using the full stock refresh")
    void testFetchEtfs_Success() {
        FetchAndStoreStockDataUseCase.Result successResult = new FetchAndStoreStockDataUseCase.Result.Success(
            true, 4, "Successfully fetched and stored ETFs"
        );

        when(fetchAndStoreStockDataUseCase.fetchAndStoreEtfs(any(StockFilter.class)))
            .thenReturn(Uni.createFrom().item(successResult));

        Response result = adminResource.fetchEtfs(List.of("Germany"), List.of("XETR"))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) result.getEntity();
        assertEquals(true, responseEntity.get("success"));
        assertEquals(4, responseEntity.get("recordsProcessed"));

        ArgumentCaptor<StockFilter> filterCaptor = ArgumentCaptor.forClass(StockFilter.class);
        verify(fetchAndStoreStockDataUseCase).fetchAndStoreEtfs(filterCaptor.capture());
        verify(fetchAndStoreStockDataUseCase, never()).fetchAndStoreStocks(any(StockFilter.class));
        assertEquals(List.of("Germany"), filterCaptor.getValue().countries());
        assertEquals(List.of("XETR"), filterCaptor.getValue().exchanges());
    }
}
