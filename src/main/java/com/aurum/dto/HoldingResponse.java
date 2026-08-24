package com.aurum.dto;

import java.math.BigDecimal;

public class HoldingResponse {

    private final Long assetId;
    private final String symbol;
    private final String name;
    private final BigDecimal quantity;
    private final BigDecimal averageCost;
    private final BigDecimal currentPrice;
    private final BigDecimal marketValue;
    private final BigDecimal unrealisedProfitLoss;
    private final BigDecimal realisedProfitLoss;

    public HoldingResponse(
            Long assetId,
            String symbol,
            String name,
            BigDecimal quantity,
            BigDecimal averageCost,
            BigDecimal currentPrice,
            BigDecimal marketValue,
            BigDecimal unrealisedProfitLoss,
            BigDecimal realisedProfitLoss
    ) {
        this.assetId = assetId;
        this.symbol = symbol;
        this.name = name;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.currentPrice = currentPrice;
        this.marketValue = marketValue;
        this.unrealisedProfitLoss = unrealisedProfitLoss;
        this.realisedProfitLoss = realisedProfitLoss;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getUnrealisedProfitLoss() {
        return unrealisedProfitLoss;
    }

    public BigDecimal getRealisedProfitLoss() {
        return realisedProfitLoss;
    }
}