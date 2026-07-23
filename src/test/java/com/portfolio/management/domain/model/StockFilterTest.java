package com.portfolio.management.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockFilterTest {

    @Test
    @DisplayName("Should create empty filter")
    void shouldCreateEmptyFilter() {
        StockFilter filter = StockFilter.empty();

        assertThat(filter.countries()).isEmpty();
        assertThat(filter.exchanges()).isEmpty();
        assertThat(filter.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should not be empty when country filter is present")
    void shouldNotBeEmptyWhenCountryFilterIsPresent() {
        StockFilter filter = new StockFilter(List.of("United States"), List.of());

        assertThat(filter.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Should not be empty when exchange filter is present")
    void shouldNotBeEmptyWhenExchangeFilterIsPresent() {
        StockFilter filter = new StockFilter(List.of(), List.of("NASDAQ"));

        assertThat(filter.isEmpty()).isFalse();
    }
}
