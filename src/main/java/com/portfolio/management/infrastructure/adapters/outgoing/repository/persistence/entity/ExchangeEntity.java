package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing stock exchanges (NYSE, NASDAQ, LSE, etc.)
 */
@Entity
@Table(name = "exchanges",
       indexes = {
           @Index(name = "idx_exchanges_code", columnList = "code"),
           @Index(name = "idx_exchanges_country", columnList = "country"),
           @Index(name = "idx_exchanges_active", columnList = "is_active"),
           @Index(name = "idx_exchanges_currency", columnList = "currency_code")
       })
public class ExchangeEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "exchanges_seq", sequenceName = "exchanges_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exchanges_seq")
    public Long id;
    
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "currency_code", length = 10)
    private String currencyCode;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ExchangeEntity() {}
    
    public ExchangeEntity(String code, String name, String country, String timezone, String currencyCode) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.timezone = timezone;
        this.currencyCode = currencyCode;
        this.isActive = true;
    }
    
    // Getters
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
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
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "ExchangeEntity{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", name='" + name + '\'' +
               ", country='" + country + '\'' +
               ", timezone='" + timezone + '\'' +
               ", currencyCode='" + currencyCode + '\'' +
               ", isActive=" + isActive +
               ", createdAt=" + createdAt +
               '}';
    }
}
