package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity for the stock_popularity survivor table: keyed by symbol so it is
 * unaffected by the full delete-all/re-insert ingestion performs on the stocks table.
 */
@Entity
@Table(name = "stock_popularity")
public class StockPopularityEntity extends PanacheEntityBase {

    @Id
    @Column(name = "symbol", length = 50)
    public String symbol;

    @Column(name = "static_score", nullable = false)
    private Double staticScore;

    @Column(name = "tx_count", nullable = false)
    private Long txCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StockPopularityEntity() {
    }

    public Double getStaticScore() {
        return staticScore;
    }

    public void setStaticScore(Double staticScore) {
        this.staticScore = staticScore;
    }

    public Long getTxCount() {
        return txCount;
    }

    public void setTxCount(Long txCount) {
        this.txCount = txCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
