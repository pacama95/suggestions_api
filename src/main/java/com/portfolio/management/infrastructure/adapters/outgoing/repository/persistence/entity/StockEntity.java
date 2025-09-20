package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing stock data from TwelveData API
 */
@Entity
@Table(name = "stocks",
        indexes = {
                @Index(name = "idx_stock_symbol", columnList = "symbol"),
                @Index(name = "idx_stock_name", columnList = "name"),
                @Index(name = "idx_stock_exchange", columnList = "exchange"),
                @Index(name = "idx_stock_country", columnList = "country"),
                @Index(name = "idx_stock_active", columnList = "is_active"),
                @Index(name = "idx_stock_symbol_name", columnList = "symbol, name")
        })
public class StockEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "stocks_seq", sequenceName = "stocks_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stocks_seq")
    public Long id;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "exchange", length = 100)
    private String exchange;

    @Column(name = "mic_code", length = 20)
    private String micCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "figi_code", length = 50)
    private String figiCode;

    @Column(name = "cfi_code", length = 20)
    private String cfiCode;

    @Column(name = "isin", length = 50)
    private String isin;

    @Column(name = "cusip", length = 50)
    private String cusip;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "data_version", nullable = false)
    private Long dataVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public StockEntity() {
    }

    public StockEntity(String symbol, String name, String currency, String exchange,
                       String micCode, String country, String type, String figiCode,
                       String cfiCode, String isin, String cusip, Long dataVersion) {
        this.symbol = symbol;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.micCode = micCode;
        this.country = country;
        this.type = type;
        this.figiCode = figiCode;
        this.cfiCode = cfiCode;
        this.isin = isin;
        this.cusip = cusip;
        this.dataVersion = dataVersion;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters - id is inherited from PanacheEntity

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMicCode() {
        return micCode;
    }

    public void setMicCode(String micCode) {
        this.micCode = micCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFigiCode() {
        return figiCode;
    }

    public void setFigiCode(String figiCode) {
        this.figiCode = figiCode;
    }

    public String getCfiCode() {
        return cfiCode;
    }

    public void setCfiCode(String cfiCode) {
        this.cfiCode = cfiCode;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(Long dataVersion) {
        this.dataVersion = dataVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
