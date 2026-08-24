package com.aurum.dto;

import com.aurum.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {

    private final Long id;
    private final Long portfolioId;
    private final Long assetId;
    private final String assetSymbol;
    private final TransactionType type;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final LocalDate transactionDate;
    private final LocalDateTime createdAt;

    public TransactionResponse(
            Long id,
            Long portfolioId,
            Long assetId,
            String assetSymbol,
            TransactionType type,
            BigDecimal quantity,
            BigDecimal price,
            LocalDate transactionDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.assetSymbol = assetSymbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetSymbol() {
        return assetSymbol;
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