package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing currencies (USD, EUR, JPY, etc.)
 */
@Entity
@Table(name = "currencies",
       indexes = {
           @Index(name = "idx_currencies_code", columnList = "code"),
           @Index(name = "idx_currencies_active", columnList = "is_active"),
           @Index(name = "idx_currencies_country", columnList = "country_code")
       })
public class CurrencyEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "currencies_seq", sequenceName = "currencies_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currencies_seq")
    public Long id;
    
    @Column(name = "code", nullable = false, length = 10, unique = true)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "symbol", length = 10)
    private String symbol;
    
    @Column(name = "country_code", length = 10)
    private String countryCode;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public CurrencyEntity() {}
    
    public CurrencyEntity(String code, String name, String symbol, String countryCode) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.countryCode = countryCode;
        this.isActive = true;
    }
    
    // Getters
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "CurrencyEntity{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", name='" + name + '\'' +
               ", symbol='" + symbol + '\'' +
               ", countryCode='" + countryCode + '\'' +
               ", isActive=" + isActive +
               ", createdAt=" + createdAt +
               '}';
    }
}
