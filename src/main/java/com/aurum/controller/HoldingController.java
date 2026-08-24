package com.aurum.controller;

import com.aurum.dto.HoldingResponse;
import com.aurum.service.HoldingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping
    public List<HoldingResponse> getHoldings(
            @PathVariable Long portfolioId
    ) {
        return holdingService.getHoldings(portfolioId);
    }
}