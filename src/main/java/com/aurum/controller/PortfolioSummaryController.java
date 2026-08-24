package com.aurum.controller;

import com.aurum.dto.PortfolioSummaryResponse;
import com.aurum.service.PortfolioSummaryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/summary")
public class PortfolioSummaryController {

    private final PortfolioSummaryService portfolioSummaryService;

    public PortfolioSummaryController(
            PortfolioSummaryService portfolioSummaryService
    ) {
        this.portfolioSummaryService = portfolioSummaryService;
    }

    @GetMapping
    public PortfolioSummaryResponse getSummary(
            @PathVariable Long portfolioId
    ) {
        return portfolioSummaryService.getSummary(portfolioId);
    }
}