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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            PortfolioRepository portfolioRepository,
            AssetRepository assetRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
    }

    public TransactionResponse createTransaction(
            Long portfolioId,
            TransactionRequest request
    ) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() ->
                        new PortfolioNotFoundException(portfolioId));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() ->
                        new AssetNotFoundException(request.getAssetId()));

        if (request.getType() == TransactionType.SELL) {
            validateSell(
                    portfolioId,
                    asset,
                    request.getQuantity()
            );
        }

        Transaction transaction = new Transaction(
                portfolio,
                asset,
                request.getType(),
                request.getQuantity(),
                request.getPrice(),
                request.getTransactionDate()
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return toResponse(savedTransaction);
    }

    public List<TransactionResponse> getTransactionsForPortfolio(
            Long portfolioId
    ) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }

        return transactionRepository
                .findByPortfolioIdOrderByTransactionDateAscIdAsc(portfolioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateSell(
            Long portfolioId,
            Asset asset,
            BigDecimal requestedQuantity
    ) {
        List<Transaction> transactions =
                transactionRepository
                        .findByPortfolioIdAndAssetIdOrderByTransactionDateAsc(
                                portfolioId,
                                asset.getId()
                        );

        BigDecimal heldQuantity = transactions.stream()
                .map(transaction ->
                        transaction.getType() == TransactionType.BUY
                                ? transaction.getQuantity()
                                : transaction.getQuantity().negate()
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (heldQuantity.compareTo(requestedQuantity) < 0) {
            throw new InsufficientHoldingsException(
                    asset.getSymbol(),
                    heldQuantity,
                    requestedQuantity
            );
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getPortfolio().getId(),
                transaction.getAsset().getId(),
                transaction.getAsset().getSymbol(),
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getPrice(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt()
        );
    }
}
