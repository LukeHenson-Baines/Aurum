package com.aurum.dto;

import java.math.BigDecimal;

public class PortfolioSummaryResponse {

    private final Long portfolioId;
    private final String portfolioName;
    private final BigDecimal totalMarketValue;
    private final BigDecimal totalCostBasis;
    private final BigDecimal totalUnrealisedProfitLoss;
    private final BigDecimal totalRealisedProfitLoss;
    private final BigDecimal totalProfitLoss;
    private final BigDecimal returnPercentage;

    public PortfolioSummaryResponse(
            Long portfolioId,
            String portfolioName,
            BigDecimal totalMarketValue,
            BigDecimal totalCostBasis,
            BigDecimal totalUnrealisedProfitLoss,
            BigDecimal totalRealisedProfitLoss,
            BigDecimal totalProfitLoss,
            BigDecimal returnPercentage
    ) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.totalMarketValue = totalMarketValue;
        this.totalCostBasis = totalCostBasis;
        this.totalUnrealisedProfitLoss = totalUnrealisedProfitLoss;
        this.totalRealisedProfitLoss = totalRealisedProfitLoss;
        this.totalProfitLoss = totalProfitLoss;
        this.returnPercentage = returnPercentage;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public BigDecimal getTotalMarketValue() {
        return totalMarketValue;
    }

    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }

    public BigDecimal getTotalUnrealisedProfitLoss() {
        return totalUnrealisedProfitLoss;
    }

    public BigDecimal getTotalRealisedProfitLoss() {
        return totalRealisedProfitLoss;
    }

    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public BigDecimal getReturnPercentage() {
        return returnPercentage;
    }
}