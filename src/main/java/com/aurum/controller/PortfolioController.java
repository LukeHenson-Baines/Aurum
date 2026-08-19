package com.aurum.controller;

import com.aurum.dto.PortfolioRequest;
import com.aurum.dto.PortfolioResponse;
import com.aurum.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse createPortfolio(
            @Valid @RequestBody PortfolioRequest request) {

        return portfolioService.createPortfolio(request);
    }

    @GetMapping
    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    @GetMapping("/{id}")
    public PortfolioResponse getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    @PutMapping("/{id}")
    public PortfolioResponse updatePortfolio(
        @PathVariable Long id,
        @Valid @RequestBody PortfolioRequest request) {

    return portfolioService.updatePortfolio(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }
}