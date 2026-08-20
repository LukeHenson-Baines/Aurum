package com.aurum.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AssetRequest {

    @NotBlank(message = "Asset symbol is required")
    @Size(max = 20, message = "Asset symbol must not exceed 20 characters")
    private String symbol;

    @NotBlank(message = "Asset name is required")
    @Size(max = 150, message = "Asset name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Current price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Current price must be greater than zero"
    )
    private BigDecimal currentPrice;

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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}