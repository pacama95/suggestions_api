package com.portfolio.management.application.service;

import com.portfolio.management.domain.port.outgoing.StockPort;
import com.portfolio.management.infrastructure.adapters.outgoing.client.MarketDataAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FetchAndStoreStockDataServiceTest {

    @Mock
    MarketDataAdapter mockTwelveDataService;

    @Mock
    StockPort stockPort;

    private FetchAndStoreStockDataService stockDataService;

    @BeforeEach
    void setUp() {
        stockDataService = new FetchAndStoreStockDataService(mockTwelveDataService, stockPort);

    }
}
