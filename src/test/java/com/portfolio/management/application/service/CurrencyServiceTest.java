package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.domain.port.incoming.GetCurrenciesUseCase;
import com.portfolio.management.domain.port.incoming.GetCurrencyUseCase;
import com.portfolio.management.domain.port.outgoing.CurrencyRepository;
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
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository mockRepository;

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService(mockRepository);
    }

    @Test
    void shouldReturnSuccessWithCurrenciesWhenValidSearchWithAllFilters() {
        // Given
        String searchTerm = "Dollar";
        String countryCode = "US";
        boolean majorOnly = false;
        var currencies = List.of(
                Currency.of(1L, "USD", "US Dollar", "$", "US", true),
                Currency.of(2L, "CAD", "Canadian Dollar", "C$", "CA", true)
        );
        var query = new GetCurrenciesUseCase.Query(searchTerm, countryCode, majorOnly);

        when(mockRepository.findByCountryCode(countryCode)).thenReturn(Uni.createFrom().item(currencies));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.Success.class);
        var successResult = (GetCurrenciesUseCase.Result.Success) result;
        assertThat(successResult.currencies()).hasSize(2);
        assertThat(successResult.count()).isEqualTo(2);
        assertThat(successResult.currencies().getFirst().code()).isEqualTo("USD");

        verify(mockRepository).findByCountryCode(eq(countryCode));
    }

    @Test
    void shouldReturnSuccessWithCurrenciesWhenSearchWithPartialFilters() {
        // Given
        String searchTerm = "Euro";
        var currencies = List.of(
                Currency.of(1L, "EUR", "Euro", "€", "EU", true)
        );
        var query = new GetCurrenciesUseCase.Query(searchTerm);

        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().item(currencies));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.Success.class);
        var successResult = (GetCurrenciesUseCase.Result.Success) result;
        assertThat(successResult.currencies()).hasSize(1);
        assertThat(successResult.currencies().getFirst().code()).isEqualTo("EUR");

        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnSuccessWithCurrencyWhenValidCurrencyCode() {
        // Given
        String currencyCode = "USD";
        var currency = Currency.of(1L, currencyCode, "US Dollar", "$", "US", true);
        var query = GetCurrencyUseCase.Query.byCode(currencyCode);

        when(mockRepository.findByCode(currencyCode)).thenReturn(Uni.createFrom().item(Optional.of(currency)));

        // When
        GetCurrencyUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrencyUseCase.Result.Success.class);
        var successResult = (GetCurrencyUseCase.Result.Success) result;
        assertThat(successResult.currency().code()).isEqualTo(currencyCode);
        assertThat(successResult.currency().name()).isEqualTo("US Dollar");

        verify(mockRepository).findByCode(eq(currencyCode));
    }

    @Test
    void shouldReturnOnlyMajorCurrenciesWhenMajorOnlyFilterIsTrue() {
        // Given
        var majorCurrencies = List.of(
                Currency.of(1L, "USD", "US Dollar", "$", "US", true),
                Currency.of(2L, "EUR", "Euro", "€", "EU", true),
                Currency.of(3L, "JPY", "Japanese Yen", "¥", "JP", true)
        );
        var query = GetCurrenciesUseCase.Query.majorCurrenciesOnly();

        when(mockRepository.findMajorCurrencies()).thenReturn(Uni.createFrom().item(majorCurrencies));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.Success.class);
        var successResult = (GetCurrenciesUseCase.Result.Success) result;
        assertThat(successResult.currencies()).hasSize(3);
        assertThat(successResult.count()).isEqualTo(3);
        assertThat(successResult.currencies()).allMatch(Currency::isMajorCurrency);

        verify(mockRepository).findMajorCurrencies();
    }

    @Test
    void shouldReturnCurrenciesFilteredByCountryWhenCountryProvided() {
        // Given
        String countryCode = "US";
        var currencies = List.of(
                Currency.of(1L, "USD", "US Dollar", "$", "US", true)
        );
        var query = GetCurrenciesUseCase.Query.byCountryCode(countryCode);

        when(mockRepository.findByCountryCode(countryCode)).thenReturn(Uni.createFrom().item(currencies));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.Success.class);
        var successResult = (GetCurrenciesUseCase.Result.Success) result;
        assertThat(successResult.currencies()).hasSize(1);
        assertThat(successResult.currencies().getFirst().countryCode()).isEqualTo(countryCode);

        verify(mockRepository).findByCountryCode(eq(countryCode));
    }

    @Test
    void shouldReturnAllCurrenciesWhenNoFiltersApplied() {
        // Given
        var currencies = List.of(
                Currency.of(1L, "USD", "US Dollar", "$", "US", true),
                Currency.of(2L, "EUR", "Euro", "€", "EU", true),
                Currency.of(3L, "JPY", "Japanese Yen", "¥", "JP", true),
                Currency.of(4L, "GBP", "British Pound", "£", "GB", true)
        );
        var query = new GetCurrenciesUseCase.Query();

        when(mockRepository.findAll()).thenReturn(Uni.createFrom().item(currencies));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.Success.class);
        var successResult = (GetCurrenciesUseCase.Result.Success) result;
        assertThat(successResult.currencies()).hasSize(4);
        assertThat(successResult.count()).isEqualTo(4);

        verify(mockRepository).findAll();
    }

    @Test
    void shouldReturnSuccessWithCurrencyWhenValidId() {
        // Given
        Long currencyId = 1L;
        var currency = Currency.of(currencyId, "EUR", "Euro", "€", "EU", true);
        var query = GetCurrencyUseCase.Query.byId(currencyId);

        when(mockRepository.findById(currencyId)).thenReturn(Uni.createFrom().item(Optional.of(currency)));

        // When
        GetCurrencyUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrencyUseCase.Result.Success.class);
        var successResult = (GetCurrencyUseCase.Result.Success) result;
        assertThat(successResult.currency().id()).isEqualTo(currencyId);
        assertThat(successResult.currency().code()).isEqualTo("EUR");

        verify(mockRepository).findById(eq(currencyId));
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsException() {
        // Given
        String searchTerm = "Dollar";
        var query = new GetCurrenciesUseCase.Query(searchTerm);

        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetCurrenciesUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrenciesUseCase.Result.SystemError.class);
        var errorResult = (GetCurrenciesUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().getFirst().field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().getFirst().code()).isEqualTo("SYSTEM_ERROR");

        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnNotFoundWhenCurrencyDoesNotExist() {
        // Given
        String nonExistentCode = "NONEXISTENT";
        var query = GetCurrencyUseCase.Query.byCode(nonExistentCode);

        when(mockRepository.findByCode(nonExistentCode)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        GetCurrencyUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrencyUseCase.Result.NotFound.class);

        verify(mockRepository).findByCode(eq(nonExistentCode));
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsExceptionForSingleCurrency() {
        // Given
        Long currencyId = 1L;
        var query = GetCurrencyUseCase.Query.byId(currencyId);

        when(mockRepository.findById(currencyId)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetCurrencyUseCase.Result result = currencyService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetCurrencyUseCase.Result.SystemError.class);
        var errorResult = (GetCurrencyUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().getFirst().field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().getFirst().code()).isEqualTo("SYSTEM_ERROR");

        verify(mockRepository).findById(eq(currencyId));
    }
}
