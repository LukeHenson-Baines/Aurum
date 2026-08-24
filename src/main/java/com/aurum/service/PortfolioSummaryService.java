package com.aurum.service;

import com.aurum.dto.HoldingResponse;
import com.aurum.dto.PortfolioSummaryResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Portfolio;
import com.aurum.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PortfolioSummaryService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingService holdingService;

    public PortfolioSummaryService(
            PortfolioRepository portfolioRepository,
            HoldingService holdingService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.holdingService = holdingService;
    }

    public PortfolioSummaryResponse getSummary(Long portfolioId) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() ->
                        new PortfolioNotFoundException(portfolioId));

        List<HoldingResponse> holdings =
                holdingService.getAllCalculatedHoldings(portfolioId);

        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalUnrealisedProfitLoss = BigDecimal.ZERO;
        BigDecimal totalRealisedProfitLoss = BigDecimal.ZERO;

        for (HoldingResponse holding : holdings) {

            totalMarketValue = totalMarketValue.add(
                    holding.getMarketValue()
            );

            BigDecimal holdingCostBasis =
                    holding.getQuantity()
                            .multiply(holding.getAverageCost());

            totalCostBasis = totalCostBasis.add(
                    holdingCostBasis
            );

            totalUnrealisedProfitLoss =
                    totalUnrealisedProfitLoss.add(
                            holding.getUnrealisedProfitLoss()
                    );

            totalRealisedProfitLoss =
                    totalRealisedProfitLoss.add(
                            holding.getRealisedProfitLoss()
                    );
        }

        BigDecimal totalProfitLoss =
                totalUnrealisedProfitLoss.add(
                        totalRealisedProfitLoss
                );

        BigDecimal returnPercentage = BigDecimal.ZERO;

        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            returnPercentage = totalProfitLoss
                    .divide(
                            totalCostBasis,
                            8,
                            RoundingMode.HALF_UP
                    )
                    .multiply(new BigDecimal("100"));
        }

        return new PortfolioSummaryResponse(
                portfolio.getId(),
                portfolio.getName(),
                totalMarketValue,
                totalCostBasis,
                totalUnrealisedProfitLoss,
                totalRealisedProfitLoss,
                totalProfitLoss,
                returnPercentage
        );
    }
}