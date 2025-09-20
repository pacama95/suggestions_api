package com.portfolio.management.domain.model;

public record StockProcessingResult(Stock stock, boolean success, String message) {
}
