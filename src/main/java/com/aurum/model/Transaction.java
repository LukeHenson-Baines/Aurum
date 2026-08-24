package com.aurum.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(
            Portfolio portfolio,
            Asset asset,
            TransactionType type,
            BigDecimal quantity,
            BigDecimal price,
            LocalDate transactionDate
    ) {
        this.portfolio = portfolio;
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Asset getAsset() {
        return asset;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}