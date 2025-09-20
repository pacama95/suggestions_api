package com.portfolio.management.application.service;

import com.portfolio.management.domain.port.outgoing.StockPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvancedSuggestionServiceTest {

    @Mock
    StockPort stockPort;

    private AdvancedSuggestionService advancedSuggestionService;

    @BeforeEach
    void setUp() {
        advancedSuggestionService = new AdvancedSuggestionService(stockPort);

    }


}
