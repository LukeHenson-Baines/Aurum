package com.aurum.dto;

import com.aurum.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {

    @NotNull(message = "Asset id is required")
    private Long assetId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price must be greater than zero"
    )
    private BigDecimal price;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}