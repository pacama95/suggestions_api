package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing stock types (Common Stock, ETF, REIT, etc.)
 */
@Entity
@Table(name = "stock_types",
       indexes = {
           @Index(name = "idx_stock_types_code", columnList = "code"),
           @Index(name = "idx_stock_types_active", columnList = "is_active")
       })
public class StockTypeEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "stock_types_seq", sequenceName = "stock_types_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_types_seq")
    public Long id;
    
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public StockTypeEntity() {}
    
    public StockTypeEntity(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.isActive = true;
    }
    
    // Getters
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
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
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "StockTypeEntity{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", isActive=" + isActive +
               ", createdAt=" + createdAt +
               '}';
    }
}
