package com.aurum.service;

import com.aurum.dto.HoldingResponse;
import com.aurum.dto.PortfolioSummaryResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Portfolio;
import com.aurum.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioSummaryServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private HoldingService holdingService;

    private PortfolioSummaryService portfolioSummaryService;

    @BeforeEach
    void setUp() {
        portfolioSummaryService = new PortfolioSummaryService(
                portfolioRepository,
                holdingService
        );
    }

    @Test
    void getSummary_shouldAggregateHoldings() {
        Portfolio portfolio = portfolio();

        HoldingResponse apple = new HoldingResponse(
                1L,
                "AAPL",
                "Apple Inc.",
                new BigDecimal("10"),
                new BigDecimal("100"),
                new BigDecimal("150"),
                new BigDecimal("1500"),
                new BigDecimal("500"),
                new BigDecimal("100")
        );

        HoldingResponse microsoft = new HoldingResponse(
                2L,
                "MSFT",
                "Microsoft",
                new BigDecimal("5"),
                new BigDecimal("300"),
                new BigDecimal("400"),
                new BigDecimal("2000"),
                new BigDecimal("500"),
                new BigDecimal("50")
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(holdingService.getAllCalculatedHoldings(1L))
                .thenReturn(List.of(apple, microsoft));

        PortfolioSummaryResponse response =
                portfolioSummaryService.getSummary(1L);

        assertBigDecimalEquals("3500", response.getTotalMarketValue());
        assertBigDecimalEquals("2500", response.getTotalCostBasis());
        assertBigDecimalEquals("1000", response.getTotalUnrealisedProfitLoss());
        assertBigDecimalEquals("150", response.getTotalRealisedProfitLoss());
        assertBigDecimalEquals("1150", response.getTotalProfitLoss());
        assertBigDecimalEquals("46", response.getReturnPercentage());
    }

    @Test
    void getSummary_shouldReturnZeroValuesForEmptyPortfolio() {
        Portfolio portfolio = portfolio();

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(holdingService.getAllCalculatedHoldings(1L))
                .thenReturn(List.of());

        PortfolioSummaryResponse response =
                portfolioSummaryService.getSummary(1L);

        assertBigDecimalEquals("0", response.getTotalMarketValue());
        assertBigDecimalEquals("0", response.getTotalCostBasis());
        assertBigDecimalEquals("0", response.getTotalProfitLoss());
        assertBigDecimalEquals("0", response.getReturnPercentage());
    }

    @Test
    void getSummary_shouldThrowWhenPortfolioDoesNotExist() {
        when(portfolioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                PortfolioNotFoundException.class,
                () -> portfolioSummaryService.getSummary(999L)
        );

        verifyNoInteractions(holdingService);
    }

    private Portfolio portfolio() {
        Portfolio portfolio = new Portfolio("Growth");
        ReflectionTestUtils.setField(portfolio, "id", 1L);
        return portfolio;
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }

    @Test
    void getSummary_shouldIncludeRealisedProfitFromFullySoldPosition() {
        Portfolio portfolio = portfolio();

        HoldingResponse soldPosition = new HoldingResponse(
                1L,
                "AAPL",
                "Apple Inc.",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("150"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("400")
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(holdingService.getAllCalculatedHoldings(1L))
                .thenReturn(List.of(soldPosition));

        PortfolioSummaryResponse response =
                portfolioSummaryService.getSummary(1L);

        assertBigDecimalEquals(
                "400",
                response.getTotalRealisedProfitLoss()
        );

        assertBigDecimalEquals(
                "400",
                response.getTotalProfitLoss()
        );
    }
}