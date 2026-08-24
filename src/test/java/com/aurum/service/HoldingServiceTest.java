package com.aurum.service;

import com.aurum.dto.HoldingResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Asset;
import com.aurum.model.Portfolio;
import com.aurum.model.Transaction;
import com.aurum.model.TransactionType;
import com.aurum.repository.PortfolioRepository;
import com.aurum.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private HoldingService holdingService;

    @BeforeEach
    void setUp() {
        holdingService = new HoldingService(
                portfolioRepository,
                transactionRepository
        );
    }

    @Test
    void getHoldings_shouldCalculateSingleBuy() {
        Portfolio portfolio = portfolio();
        Asset apple = asset(
                1L,
                "AAPL",
                "Apple Inc.",
                "150.00"
        );

        Transaction buy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "100.00",
                LocalDate.of(2026, 8, 20)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(1L))
                .thenReturn(List.of(buy));

        List<HoldingResponse> holdings =
                holdingService.getHoldings(1L);

        assertEquals(1, holdings.size());

        HoldingResponse holding = holdings.get(0);

        assertEquals("AAPL", holding.getSymbol());
        assertBigDecimalEquals("10", holding.getQuantity());
        assertBigDecimalEquals("100.00", holding.getAverageCost());
        assertBigDecimalEquals("1500.00", holding.getMarketValue());
        assertBigDecimalEquals(
                "500.00",
                holding.getUnrealisedProfitLoss()
        );
        assertBigDecimalEquals(
                "0",
                holding.getRealisedProfitLoss()
        );
    }

    @Test
    void getHoldings_shouldCalculateWeightedAverageCost() {
        Portfolio portfolio = portfolio();
        Asset apple = asset(
                1L,
                "AAPL",
                "Apple Inc.",
                "150.00"
        );

        Transaction firstBuy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "100.00",
                LocalDate.of(2026, 8, 20)
        );

        Transaction secondBuy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "120.00",
                LocalDate.of(2026, 8, 21)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(1L))
                .thenReturn(List.of(firstBuy, secondBuy));

        HoldingResponse holding =
                holdingService.getHoldings(1L).get(0);

        assertBigDecimalEquals("20", holding.getQuantity());
        assertBigDecimalEquals(
                "110.00000000",
                holding.getAverageCost()
        );
        assertBigDecimalEquals(
                "3000.00",
                holding.getMarketValue()
        );
        assertBigDecimalEquals(
                "800.00000000",
                holding.getUnrealisedProfitLoss()
        );
    }

    @Test
    void getHoldings_shouldCalculateRealisedAndUnrealisedProfitLoss() {
        Portfolio portfolio = portfolio();
        Asset apple = asset(
                1L,
                "AAPL",
                "Apple Inc.",
                "150.00"
        );

        Transaction firstBuy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "100.00",
                LocalDate.of(2026, 8, 20)
        );

        Transaction secondBuy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "120.00",
                LocalDate.of(2026, 8, 21)
        );

        Transaction sell = transaction(
                portfolio,
                apple,
                TransactionType.SELL,
                "5",
                "140.00",
                LocalDate.of(2026, 8, 22)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(1L))
                .thenReturn(List.of(firstBuy, secondBuy, sell));

        HoldingResponse holding =
                holdingService.getHoldings(1L).get(0);

        // 20 bought - 5 sold
        assertBigDecimalEquals("15", holding.getQuantity());

        // (10 × 100 + 10 × 120) / 20
        assertBigDecimalEquals(
                "110.00000000",
                holding.getAverageCost()
        );

        // 15 × 150
        assertBigDecimalEquals(
                "2250.00",
                holding.getMarketValue()
        );

        // 15 × (150 - 110)
        assertBigDecimalEquals(
                "600.00000000",
                holding.getUnrealisedProfitLoss()
        );

        // 5 × (140 - 110)
        assertBigDecimalEquals(
                "150.00000000",
                holding.getRealisedProfitLoss()
        );
    }

    @Test
    void getHoldings_shouldExcludeFullySoldPosition() {
        Portfolio portfolio = portfolio();
        Asset apple = asset(
                1L,
                "AAPL",
                "Apple Inc.",
                "150.00"
        );

        Transaction buy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "100.00",
                LocalDate.of(2026, 8, 20)
        );

        Transaction sell = transaction(
                portfolio,
                apple,
                TransactionType.SELL,
                "10",
                "140.00",
                LocalDate.of(2026, 8, 21)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(1L))
                .thenReturn(List.of(buy, sell));

        List<HoldingResponse> holdings =
                holdingService.getHoldings(1L);

        assertTrue(holdings.isEmpty());
    }

    @Test
    void getHoldings_shouldCalculateMultipleAssetsSeparately() {
        Portfolio portfolio = portfolio();

        Asset apple = asset(
                1L,
                "AAPL",
                "Apple Inc.",
                "150.00"
        );

        Asset microsoft = asset(
                2L,
                "MSFT",
                "Microsoft",
                "400.00"
        );

        Transaction appleBuy = transaction(
                portfolio,
                apple,
                TransactionType.BUY,
                "10",
                "100.00",
                LocalDate.of(2026, 8, 20)
        );

        Transaction microsoftBuy = transaction(
                portfolio,
                microsoft,
                TransactionType.BUY,
                "5",
                "350.00",
                LocalDate.of(2026, 8, 21)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(1L))
                .thenReturn(List.of(appleBuy, microsoftBuy));

        List<HoldingResponse> holdings =
                holdingService.getHoldings(1L);

        assertEquals(2, holdings.size());

        assertEquals("AAPL", holdings.get(0).getSymbol());
        assertBigDecimalEquals(
                "1500.00",
                holdings.get(0).getMarketValue()
        );

        assertEquals("MSFT", holdings.get(1).getSymbol());
        assertBigDecimalEquals(
                "2000.00",
                holdings.get(1).getMarketValue()
        );
    }

    @Test
    void getHoldings_shouldThrowWhenPortfolioDoesNotExist() {
        when(portfolioRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                PortfolioNotFoundException.class,
                () -> holdingService.getHoldings(999L)
        );

        verifyNoInteractions(transactionRepository);
    }

    private Portfolio portfolio() {
        Portfolio portfolio = new Portfolio("Growth");
        ReflectionTestUtils.setField(portfolio, "id", 1L);
        return portfolio;
    }

    private Asset asset(
            Long id,
            String symbol,
            String name,
            String currentPrice
    ) {
        Asset asset = new Asset(
                symbol,
                name,
                new BigDecimal(currentPrice)
        );

        ReflectionTestUtils.setField(asset, "id", id);

        return asset;
    }

    private Transaction transaction(
            Portfolio portfolio,
            Asset asset,
            TransactionType type,
            String quantity,
            String price,
            LocalDate date
    ) {
        return new Transaction(
                portfolio,
                asset,
                type,
                new BigDecimal(quantity),
                new BigDecimal(price),
                date
        );
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
}