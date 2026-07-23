package com.portfolio.management.application.service;

import com.portfolio.management.domain.port.incoming.RecordStockUsageUseCase;
import com.portfolio.management.domain.port.outgoing.PopularityPort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordStockUsageServiceTest {

    @Mock
    PopularityPort popularityPort;

    private RecordStockUsageService service;

    @BeforeEach
    void setUp() {
        service = new RecordStockUsageService(popularityPort);
    }

    @Test
    @DisplayName("Should record usage and normalize ticker to uppercase")
    void shouldRecordUsage() {
        when(popularityPort.incrementUsage(eq("AAPL"), eq("NASDAQ"), eq("USD")))
                .thenReturn(Uni.createFrom().voidItem());

        RecordStockUsageUseCase.Result result = service.execute(new RecordStockUsageUseCase.Command("aapl", "NASDAQ", "USD"))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).isInstanceOf(RecordStockUsageUseCase.Result.Success.class);
        verify(popularityPort).incrementUsage("AAPL", "NASDAQ", "USD");
    }

    @Test
    @DisplayName("Should ignore commands with a blank ticker without touching the port")
    void shouldIgnoreBlankTicker() {
        RecordStockUsageUseCase.Result result = service.execute(new RecordStockUsageUseCase.Command("  ", "NASDAQ", "USD"))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).isInstanceOf(RecordStockUsageUseCase.Result.Ignored.class);
        verifyNoInteractions(popularityPort);
    }

    @Test
    @DisplayName("Should return error when the popularity port fails")
    void shouldReturnErrorWhenPortFails() {
        when(popularityPort.incrementUsage(eq("AAPL"), eq("NASDAQ"), eq("USD")))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB unavailable")));

        RecordStockUsageUseCase.Result result = service.execute(new RecordStockUsageUseCase.Command("AAPL", "NASDAQ", "USD"))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(result).isInstanceOf(RecordStockUsageUseCase.Result.Error.class);
        assertThat(((RecordStockUsageUseCase.Result.Error) result).message()).contains("DB unavailable");
    }
}
