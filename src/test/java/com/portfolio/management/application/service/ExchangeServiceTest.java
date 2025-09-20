package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.domain.port.incoming.GetExchangeUseCase;
import com.portfolio.management.domain.port.incoming.GetExchangesUseCase;
import com.portfolio.management.domain.port.outgoing.ExchangeRepository;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private ExchangeRepository mockRepository;

    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        exchangeService = new ExchangeService(mockRepository);
    }

    @Test
    void shouldReturnSuccessWithExchangesWhenValidSearchWithAllCriteria() {
        // Given
        String searchTerm = "NYSE";
        var exchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "NYSE_ARCA", "NYSE Arca", "United States", "America/New_York", "USD", true)
        );
        var query = new GetExchangesUseCase.Query(searchTerm);
        
        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().item(exchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(2);
        assertThat(successResult.count()).isEqualTo(2);
        assertThat(successResult.exchanges().get(0).code()).isEqualTo("NYSE");
        
        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnSuccessWithExchangesWhenFilteredByCountry() {
        // Given
        String country = "United States";
        var exchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "NASDAQ", "NASDAQ Global Market", "United States", "America/New_York", "USD", true)
        );
        var query = GetExchangesUseCase.Query.byCountry(country);
        
        when(mockRepository.findByCountry(country)).thenReturn(Uni.createFrom().item(exchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(2);
        assertThat(successResult.exchanges()).allMatch(exchange -> exchange.country().equals(country));
        
        verify(mockRepository).findByCountry(eq(country));
    }

    @Test
    void shouldReturnSuccessWithExchangeWhenValidIdentifier() {
        // Given
        Long exchangeId = 1L;
        var exchange = Exchange.of(exchangeId, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true);
        var query = GetExchangeUseCase.Query.byId(exchangeId);
        
        when(mockRepository.findById(exchangeId)).thenReturn(Uni.createFrom().item(Optional.of(exchange)));

        // When
        GetExchangeUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangeUseCase.Result.Success.class);
        var successResult = (GetExchangeUseCase.Result.Success) result;
        assertThat(successResult.exchange().id()).isEqualTo(exchangeId);
        assertThat(successResult.exchange().code()).isEqualTo("LSE");
        
        verify(mockRepository).findById(eq(exchangeId));
    }

    @Test
    void shouldReturnSuccessWithExchangesWhenFilteredByRegion() {
        // Given
        var usExchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "NASDAQ", "NASDAQ Global Market", "United States", "America/New_York", "USD", true)
        );
        var query = GetExchangesUseCase.Query.byRegion(GetExchangesUseCase.RegionFilter.US);
        
        when(mockRepository.findUSExchanges()).thenReturn(Uni.createFrom().item(usExchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(2);
        assertThat(successResult.exchanges()).allMatch(exchange -> exchange.isUSExchange());
        
        verify(mockRepository).findUSExchanges();
    }

    @Test
    void shouldReturnSuccessWithExchangesWhenFilteredByCurrency() {
        // Given
        String currencyCode = "EUR";
        var eurExchanges = List.of(
                Exchange.of(1L, "XETR", "Deutsche Boerse XETRA", "Germany", "Europe/Berlin", "EUR", true),
                Exchange.of(2L, "EPA", "Euronext Paris", "France", "Europe/Paris", "EUR", true)
        );
        var query = GetExchangesUseCase.Query.byCurrency(currencyCode);
        
        when(mockRepository.findByCurrencyCode(currencyCode)).thenReturn(Uni.createFrom().item(eurExchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(2);
        assertThat(successResult.exchanges()).allMatch(exchange -> exchange.currencyCode().equals(currencyCode));
        
        verify(mockRepository).findByCurrencyCode(eq(currencyCode));
    }

    @Test
    void shouldReturnAllExchangesWhenNoFiltersApplied() {
        // Given
        var exchanges = List.of(
                Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "America/New_York", "USD", true),
                Exchange.of(2L, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true),
                Exchange.of(3L, "TSE", "Tokyo Stock Exchange", "Japan", "Asia/Tokyo", "JPY", true)
        );
        var query = new GetExchangesUseCase.Query();
        
        when(mockRepository.findAll()).thenReturn(Uni.createFrom().item(exchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(3);
        assertThat(successResult.count()).isEqualTo(3);
        
        verify(mockRepository).findAll();
    }

    @Test
    void shouldReturnSuccessWithExchangeWhenValidCode() {
        // Given
        String exchangeCode = "NASDAQ";
        var exchange = Exchange.of(1L, exchangeCode, "NASDAQ Global Market", "United States", "America/New_York", "USD", true);
        var query = GetExchangeUseCase.Query.byCode(exchangeCode);
        
        when(mockRepository.findByCode(exchangeCode)).thenReturn(Uni.createFrom().item(Optional.of(exchange)));

        // When
        GetExchangeUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangeUseCase.Result.Success.class);
        var successResult = (GetExchangeUseCase.Result.Success) result;
        assertThat(successResult.exchange().code()).isEqualTo(exchangeCode);
        assertThat(successResult.exchange().name()).isEqualTo("NASDAQ Global Market");
        
        verify(mockRepository).findByCode(eq(exchangeCode));
    }

    @Test
    void shouldReturnSuccessWithEuropeanExchangesWhenFilteredByEuropeRegion() {
        // Given
        var europeanExchanges = List.of(
                Exchange.of(1L, "LSE", "London Stock Exchange", "United Kingdom", "Europe/London", "GBP", true),
                Exchange.of(2L, "XETR", "Deutsche Boerse XETRA", "Germany", "Europe/Berlin", "EUR", true)
        );
        var query = GetExchangesUseCase.Query.byRegion(GetExchangesUseCase.RegionFilter.EUROPE);
        
        when(mockRepository.findEuropeanExchanges()).thenReturn(Uni.createFrom().item(europeanExchanges));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.Success.class);
        var successResult = (GetExchangesUseCase.Result.Success) result;
        assertThat(successResult.exchanges()).hasSize(2);
        assertThat(successResult.exchanges()).allMatch(exchange -> exchange.isEuropeanExchange());
        
        verify(mockRepository).findEuropeanExchanges();
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsException() {
        // Given
        String searchTerm = "NYSE";
        var query = new GetExchangesUseCase.Query(searchTerm);
        
        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetExchangesUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangesUseCase.Result.SystemError.class);
        var errorResult = (GetExchangesUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().get(0).field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().get(0).code()).isEqualTo("SYSTEM_ERROR");
        
        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnNotFoundWhenExchangeDoesNotExist() {
        // Given
        String nonExistentCode = "NONEXISTENT";
        var query = GetExchangeUseCase.Query.byCode(nonExistentCode);
        
        when(mockRepository.findByCode(nonExistentCode)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        GetExchangeUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangeUseCase.Result.NotFound.class);
        
        verify(mockRepository).findByCode(eq(nonExistentCode));
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsExceptionForSingleExchange() {
        // Given
        Long exchangeId = 1L;
        var query = GetExchangeUseCase.Query.byId(exchangeId);
        
        when(mockRepository.findById(exchangeId)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetExchangeUseCase.Result result = exchangeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetExchangeUseCase.Result.SystemError.class);
        var errorResult = (GetExchangeUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().get(0).field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().get(0).code()).isEqualTo("SYSTEM_ERROR");
        
        verify(mockRepository).findById(eq(exchangeId));
    }
}
