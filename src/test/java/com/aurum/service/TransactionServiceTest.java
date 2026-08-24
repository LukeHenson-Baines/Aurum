package com.aurum.service;

import com.aurum.dto.TransactionRequest;
import com.aurum.dto.TransactionResponse;
import com.aurum.exception.AssetNotFoundException;
import com.aurum.exception.InsufficientHoldingsException;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Asset;
import com.aurum.model.Portfolio;
import com.aurum.model.Transaction;
import com.aurum.model.TransactionType;
import com.aurum.repository.AssetRepository;
import com.aurum.repository.PortfolioRepository;
import com.aurum.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private AssetRepository assetRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                portfolioRepository,
                assetRepository
        );
    }

    @Test
    void createBuyTransaction_shouldSaveTransaction() {
        Portfolio portfolio = new Portfolio("Growth");
        Asset asset = new Asset(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        TransactionRequest request = request(
                1L,
                TransactionType.BUY,
                "10",
                "180.00"
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(assetRepository.findById(1L))
                .thenReturn(Optional.of(asset));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response =
                transactionService.createTransaction(1L, request);

        assertEquals(TransactionType.BUY, response.getType());
        assertEquals(new BigDecimal("10"), response.getQuantity());
        assertEquals(new BigDecimal("180.00"), response.getPrice());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldThrowWhenPortfolioDoesNotExist() {
        TransactionRequest request = request(
                1L,
                TransactionType.BUY,
                "10",
                "180.00"
        );

        when(portfolioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                PortfolioNotFoundException.class,
                () -> transactionService.createTransaction(999L, request)
        );

        verifyNoInteractions(assetRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void createTransaction_shouldThrowWhenAssetDoesNotExist() {
        Portfolio portfolio = new Portfolio("Growth");

        TransactionRequest request = request(
                999L,
                TransactionType.BUY,
                "10",
                "180.00"
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(assetRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AssetNotFoundException.class,
                () -> transactionService.createTransaction(1L, request)
        );

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createSellTransaction_shouldAllowSaleWithinAvailableHoldings() {
        Portfolio portfolio = new Portfolio("Growth");
        Asset asset = new Asset(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        Transaction buy = new Transaction(
                portfolio,
                asset,
                TransactionType.BUY,
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 8, 20)
        );

        TransactionRequest request = request(
                1L,
                TransactionType.SELL,
                "4",
                "210.00"
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(assetRepository.findById(1L))
                .thenReturn(Optional.of(asset));

        when(transactionRepository
                .findByPortfolioIdAndAssetIdOrderByTransactionDateAsc(
                        eq(1L),
                        any()
                ))
                .thenReturn(List.of(buy));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
                () -> transactionService.createTransaction(1L, request)
        );

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createSellTransaction_shouldRejectOverselling() {
        Portfolio portfolio = new Portfolio("Growth");
        Asset asset = new Asset(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        Transaction buy = new Transaction(
                portfolio,
                asset,
                TransactionType.BUY,
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 8, 20)
        );

        Transaction sell = new Transaction(
                portfolio,
                asset,
                TransactionType.SELL,
                new BigDecimal("4"),
                new BigDecimal("210.00"),
                LocalDate.of(2026, 8, 21)
        );

        TransactionRequest request = request(
                1L,
                TransactionType.SELL,
                "7",
                "215.00"
        );

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(assetRepository.findById(1L))
                .thenReturn(Optional.of(asset));

        when(transactionRepository
                .findByPortfolioIdAndAssetIdOrderByTransactionDateAsc(
                        eq(1L),
                        any()
                ))
                .thenReturn(List.of(buy, sell));

        assertThrows(
                InsufficientHoldingsException.class,
                () -> transactionService.createTransaction(1L, request)
        );

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsForPortfolio_shouldReturnTransactions() {
        Portfolio portfolio = new Portfolio("Growth");
        Asset asset = new Asset(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        Transaction transaction = new Transaction(
                portfolio,
                asset,
                TransactionType.BUY,
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 8, 20)
        );

        when(portfolioRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findByPortfolioIdOrderByTransactionDateAsc(1L))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses =
                transactionService.getTransactionsForPortfolio(1L);

        assertEquals(1, responses.size());
        assertEquals(TransactionType.BUY, responses.get(0).getType());
    }

    private TransactionRequest request(
            Long assetId,
            TransactionType type,
            String quantity,
            String price
    ) {
        TransactionRequest request = new TransactionRequest();
        request.setAssetId(assetId);
        request.setType(type);
        request.setQuantity(new BigDecimal(quantity));
        request.setPrice(new BigDecimal(price));
        request.setTransactionDate(LocalDate.of(2026, 8, 24));

        return request;
    }
}