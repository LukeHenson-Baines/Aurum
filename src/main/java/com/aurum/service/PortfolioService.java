package com.aurum.service;

import com.aurum.dto.PortfolioRequest;
import com.aurum.dto.PortfolioResponse;
import com.aurum.model.Portfolio;
import com.aurum.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public PortfolioResponse createPortfolio(PortfolioRequest request) {
        Portfolio portfolio = new Portfolio(request.getName());

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return toResponse(savedPortfolio);
    }

    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getCreatedAt()
        );
    }
}