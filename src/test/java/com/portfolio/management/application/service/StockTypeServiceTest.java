package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.domain.port.incoming.GetStockTypeUseCase;
import com.portfolio.management.domain.port.incoming.GetStockTypesUseCase;
import com.portfolio.management.domain.port.outgoing.StockTypeRepository;
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
class StockTypeServiceTest {

    @Mock
    private StockTypeRepository mockRepository;

    private StockTypeService stockTypeService;

    @BeforeEach
    void setUp() {
        stockTypeService = new StockTypeService(mockRepository);
    }

    @Test
    void shouldReturnSuccessWithStockTypesWhenValidSearchQuery() {
        // Given
        String searchTerm = "ETF";
        var stockTypes = List.of(
                StockType.of(1L, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true),
                StockType.of(2L, "ETF_BOND", "Bond ETF", "Bond exchange traded fund", true)
        );
        var query = new GetStockTypesUseCase.Query(searchTerm);

        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().item(stockTypes));

        // When
        GetStockTypesUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypesUseCase.Result.Success.class);
        var successResult = (GetStockTypesUseCase.Result.Success) result;
        assertThat(successResult.stockTypes()).hasSize(2);
        assertThat(successResult.count()).isEqualTo(2);
        assertThat(successResult.stockTypes().get(0).code()).isEqualTo("ETF");

        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnSuccessWithAllStockTypesWhenNoSearchQuery() {
        // Given
        var stockTypes = List.of(
                StockType.of(1L, "COMMON_STOCK", "Common Stock", "Common stock shares", true),
                StockType.of(2L, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true),
                StockType.of(3L, "REIT", "Real Estate Investment Trust", "Real estate investment trust", true)
        );
        var query = new GetStockTypesUseCase.Query();

        when(mockRepository.findAll()).thenReturn(Uni.createFrom().item(stockTypes));

        // When
        GetStockTypesUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypesUseCase.Result.Success.class);
        var successResult = (GetStockTypesUseCase.Result.Success) result;
        assertThat(successResult.stockTypes()).hasSize(3);
        assertThat(successResult.count()).isEqualTo(3);

        verify(mockRepository).findAll();
    }

    @Test
    void shouldReturnSuccessWithEmptyListWhenNoStockTypesFound() {
        // Given
        String searchTerm = "NONEXISTENT";
        var query = new GetStockTypesUseCase.Query(searchTerm);

        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().item(List.of()));

        // When
        GetStockTypesUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypesUseCase.Result.Success.class);
        var successResult = (GetStockTypesUseCase.Result.Success) result;
        assertThat(successResult.stockTypes()).isEmpty();
        assertThat(successResult.count()).isEqualTo(0);

        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsException() {
        // Given
        String searchTerm = "ETF";
        var query = new GetStockTypesUseCase.Query(searchTerm);

        when(mockRepository.search(searchTerm)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetStockTypesUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypesUseCase.Result.SystemError.class);
        var errorResult = (GetStockTypesUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().get(0).field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().get(0).code()).isEqualTo("SYSTEM_ERROR");

        verify(mockRepository).search(eq(searchTerm));
    }

    @Test
    void shouldReturnSuccessWithStockTypeWhenValidIdQuery() {
        // Given
        Long stockTypeId = 1L;
        var stockType = StockType.of(stockTypeId, "ETF", "Exchange Traded Fund", "Investment fund traded on exchanges", true);
        var query = GetStockTypeUseCase.Query.byId(stockTypeId);

        when(mockRepository.findById(stockTypeId)).thenReturn(Uni.createFrom().item(Optional.of(stockType)));

        // When
        GetStockTypeUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypeUseCase.Result.Success.class);
        var successResult = (GetStockTypeUseCase.Result.Success) result;
        assertThat(successResult.stockType().id()).isEqualTo(stockTypeId);
        assertThat(successResult.stockType().code()).isEqualTo("ETF");

        verify(mockRepository).findById(eq(stockTypeId));
    }

    @Test
    void shouldReturnSuccessWithStockTypeWhenValidCodeQuery() {
        // Given
        String stockTypeCode = "REIT";
        var stockType = StockType.of(1L, stockTypeCode, "Real Estate Investment Trust", "Real estate investment trust", true);
        var query = GetStockTypeUseCase.Query.byCode(stockTypeCode);

        when(mockRepository.findByCode(stockTypeCode)).thenReturn(Uni.createFrom().item(Optional.of(stockType)));

        // When
        GetStockTypeUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypeUseCase.Result.Success.class);
        var successResult = (GetStockTypeUseCase.Result.Success) result;
        assertThat(successResult.stockType().code()).isEqualTo(stockTypeCode);
        assertThat(successResult.stockType().name()).isEqualTo("Real Estate Investment Trust");

        verify(mockRepository).findByCode(eq(stockTypeCode));
    }

    @Test
    void shouldReturnNotFoundWhenStockTypeDoesNotExistById() {
        // Given
        Long nonExistentId = 999L;
        var query = GetStockTypeUseCase.Query.byId(nonExistentId);

        when(mockRepository.findById(nonExistentId)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        GetStockTypeUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypeUseCase.Result.NotFound.class);

        verify(mockRepository).findById(eq(nonExistentId));
    }

    @Test
    void shouldReturnNotFoundWhenStockTypeDoesNotExistByCode() {
        // Given
        String nonExistentCode = "NONEXISTENT";
        var query = GetStockTypeUseCase.Query.byCode(nonExistentCode);

        when(mockRepository.findByCode(nonExistentCode)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        GetStockTypeUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypeUseCase.Result.NotFound.class);

        verify(mockRepository).findByCode(eq(nonExistentCode));
    }

    @Test
    void shouldReturnSystemErrorWhenRepositoryThrowsExceptionForSingleStockType() {
        // Given
        Long stockTypeId = 1L;
        var query = GetStockTypeUseCase.Query.byId(stockTypeId);

        when(mockRepository.findById(stockTypeId)).thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When
        GetStockTypeUseCase.Result result = stockTypeService.execute(query)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        // Then
        assertThat(result).isInstanceOf(GetStockTypeUseCase.Result.SystemError.class);
        var errorResult = (GetStockTypeUseCase.Result.SystemError) result;
        assertThat(errorResult.errors().errors()).hasSize(1);
        assertThat(errorResult.errors().errors().get(0).field()).isEqualTo("system");
        assertThat(errorResult.errors().errors().get(0).code()).isEqualTo("SYSTEM_ERROR");

        verify(mockRepository).findById(eq(stockTypeId));
    }
}
