package com.portfolio.management.infrastructure.adapters.incoming.web;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.model.Errors;
import com.portfolio.management.domain.port.incoming.GetExchangeUseCase;
import com.portfolio.management.domain.port.incoming.GetExchangesUseCase;
import com.portfolio.management.infrastructure.adapters.incoming.web.dto.ExchangeResponse;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ErrorMapper;
import com.portfolio.management.infrastructure.adapters.incoming.web.mapper.ExchangeWebMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeResourceTest {

    @Mock
    private GetExchangesUseCase mockGetExchangesUseCase;
    
    @Mock
    private GetExchangeUseCase mockGetExchangeUseCase;
    
    @Mock
    private ExchangeWebMapper mockExchangeMapper;
    
    @Mock
    private ErrorMapper mockErrorMapper;

    private ExchangeResource exchangeResource;

    @BeforeEach
    void setUp() {
        exchangeResource = new ExchangeResource(
                mockGetExchangesUseCase,
                mockGetExchangeUseCase,
                mockExchangeMapper,
                mockErrorMapper
        );
    }

    @Test
    void shouldReturnExchangesWhenValidRequest() {
        // Given
        String search = "NYSE";
        String country = "United States";
        String currency = "USD";
        String region = "US";
        
        var exchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true)
        );
        var exchangeResponse = new ExchangeResponse(List.of(), 1);
        var successResult = new GetExchangesUseCase.Result.Success(exchanges, 1);

        when(mockGetExchangesUseCase.execute(any(GetExchangesUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(successResult));
        when(mockExchangeMapper.toExchangeResponse(anyList())).thenReturn(exchangeResponse);

        // When
        Response result = exchangeResource.getExchanges(search, country, currency, region)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(result.getEntity()).isInstanceOf(ExchangeResponse.class);
        
        verify(mockGetExchangesUseCase).execute(any(GetExchangesUseCase.Query.class));
        verify(mockExchangeMapper).toExchangeResponse(eq(exchanges));
    }

    @Test
    void shouldReturnExchangeWhenValidIdentifier() {
        // Given
        Long exchangeId = 1L;
        var exchange = Exchange.of(exchangeId, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true);
        var exchangeResponse = new ExchangeResponse(List.of(), 1);
        var successResult = new GetExchangeUseCase.Result.Success(exchange);

        when(mockGetExchangeUseCase.execute(any(GetExchangeUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(successResult));
        when(mockExchangeMapper.toExchangeResponse(any(Exchange.class))).thenReturn(exchangeResponse);

        // When
        Response result = exchangeResource.getExchangeById(exchangeId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(result.getEntity()).isInstanceOf(ExchangeResponse.class);
        
        verify(mockGetExchangeUseCase).execute(any(GetExchangeUseCase.Query.class));
        verify(mockExchangeMapper).toExchangeResponse(eq(exchange));
    }

    @Test
    void shouldReturnExchangeWhenValidCode() {
        // Given
        String exchangeCode = "NASDAQ";
        var exchange = Exchange.of(1L, exchangeCode, "NASDAQ Global Market", "United States", "America/New_York", "USD", true);
        var exchangeResponse = new ExchangeResponse(List.of(), 1);
        var successResult = new GetExchangeUseCase.Result.Success(exchange);

        when(mockGetExchangeUseCase.execute(any(GetExchangeUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(successResult));
        when(mockExchangeMapper.toExchangeResponse(any(Exchange.class))).thenReturn(exchangeResponse);

        // When
        Response result = exchangeResource.getExchangeByCode(exchangeCode)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(result.getEntity()).isInstanceOf(ExchangeResponse.class);
        
        verify(mockGetExchangeUseCase).execute(any(GetExchangeUseCase.Query.class));
        verify(mockExchangeMapper).toExchangeResponse(eq(exchange));
    }

    @Test
    void shouldReturnFilteredExchangesWhenFiltersApplied() {
        // Given
        String country = "Germany";
        var exchanges = List.of(
                Exchange.of(1L, "XETR", "Deutsche Boerse XETRA", "Germany", "Europe/Berlin", "EUR", true)
        );
        var exchangeResponse = new ExchangeResponse(List.of(), 1);
        var successResult = new GetExchangesUseCase.Result.Success(exchanges, 1);

        when(mockGetExchangesUseCase.execute(any(GetExchangesUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(successResult));
        when(mockExchangeMapper.toExchangeResponse(anyList())).thenReturn(exchangeResponse);

        // When
        Response result = exchangeResource.getExchanges(null, country, null, null)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        
        verify(mockGetExchangesUseCase).execute(any(GetExchangesUseCase.Query.class));
        verify(mockExchangeMapper).toExchangeResponse(eq(exchanges));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidRegionProvided() {
        // Given
        String invalidRegion = "INVALID_REGION";

        // When
        Response result = exchangeResource.getExchanges(null, null, null, invalidRegion)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verify(mockErrorMapper).toErrorResponse(eq("country"), anyString(), eq("INVALID_REGION"));
    }

    @Test
    void shouldReturnBadRequestWhenNullIdProvided() {
        // Given
        Long nullId = null;

        // When
        Response result = exchangeResource.getExchangeById(nullId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verify(mockErrorMapper).toErrorResponse(eq("id"), eq("Exchange ID is required"), eq("MISSING_ID"));
    }

    @Test
    void shouldReturnBadRequestWhenEmptyCodeProvided() {
        // Given
        String emptyCode = "";

        // When
        Response result = exchangeResource.getExchangeByCode(emptyCode)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verify(mockErrorMapper).toErrorResponse(eq("code"), eq("Exchange code is required"), eq("MISSING_CODE"));
    }

    @Test
    void shouldReturnErrorResponseWhenUseCaseReturnsError() {
        // Given
        String search = "TEST";
        var errors = Errors.of("system", "Database error", "SYSTEM_ERROR");
        var systemErrorResult = new GetExchangesUseCase.Result.SystemError(errors);

        when(mockGetExchangesUseCase.execute(any(GetExchangesUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(systemErrorResult));

        // When
        Response result = exchangeResource.getExchanges(search, null, null, null)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        
        verify(mockGetExchangesUseCase).execute(any(GetExchangesUseCase.Query.class));
        verify(mockErrorMapper).toErrorResponse(eq(errors));
    }

    @Test
    void shouldReturn404WhenExchangeNotFound() {
        // Given
        Long nonExistentId = 999L;
        var notFoundResult = new GetExchangeUseCase.Result.NotFound();

        when(mockGetExchangeUseCase.execute(any(GetExchangeUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(notFoundResult));

        // When
        Response result = exchangeResource.getExchangeById(nonExistentId)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        
        verify(mockGetExchangeUseCase).execute(any(GetExchangeUseCase.Query.class));
        verify(mockErrorMapper).toErrorResponse(eq("exchange"), eq("Exchange not found"), eq("NOT_FOUND"));
    }

    @Test
    void shouldHandleValidationError() {
        // Given
        String search = "INVALID";
        var errors = Errors.of("query", "Invalid search query", "VALIDATION_ERROR");
        var validationErrorResult = new GetExchangesUseCase.Result.ValidationError(errors);

        when(mockGetExchangesUseCase.execute(any(GetExchangesUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(validationErrorResult));

        // When
        Response result = exchangeResource.getExchanges(search, null, null, null)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verify(mockGetExchangesUseCase).execute(any(GetExchangesUseCase.Query.class));
        verify(mockErrorMapper).toErrorResponse(eq(errors));
    }

    @Test
    void shouldHandleRegionFilterCorrectly() {
        // Given
        String region = "EUROPE";
        var exchanges = List.of(
                Exchange.of(1L, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true)
        );
        var exchangeResponse = new ExchangeResponse(List.of(), 1);
        var successResult = new GetExchangesUseCase.Result.Success(exchanges, 1);

        when(mockGetExchangesUseCase.execute(any(GetExchangesUseCase.Query.class)))
                .thenReturn(Uni.createFrom().item(successResult));
        when(mockExchangeMapper.toExchangeResponse(anyList())).thenReturn(exchangeResponse);

        // When
        Response result = exchangeResource.getExchanges(null, null, null, region)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        
        verify(mockGetExchangesUseCase).execute(any(GetExchangesUseCase.Query.class));
        verify(mockExchangeMapper).toExchangeResponse(eq(exchanges));
    }
}
