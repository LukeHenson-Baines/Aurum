package com.aurum.dto;

import java.math.BigDecimal;

public class AssetResponse {

    private final Long id;
    private final String symbol;
    private final String name;
    private final BigDecimal currentPrice;

    public AssetResponse(
            Long id,
            String symbol,
            String name,
            BigDecimal currentPrice
    ) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
}