package com.aurum.service;

import com.aurum.dto.HoldingResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Asset;
import com.aurum.model.Transaction;
import com.aurum.model.TransactionType;
import com.aurum.repository.PortfolioRepository;
import com.aurum.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoldingService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    public HoldingService(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<HoldingResponse> getHoldings(Long portfolioId) {
        return calculateHoldings(portfolioId)
                .stream()
                .filter(holding ->
                        holding.getQuantity().compareTo(BigDecimal.ZERO) > 0
                )
                .toList();
    }

    public List<HoldingResponse> getAllCalculatedHoldings(Long portfolioId) {
        return calculateHoldings(portfolioId);
    }

    private List<HoldingResponse> calculateHoldings(Long portfolioId) {

        if (!portfolioRepository.existsById(portfolioId)) {
            throw new PortfolioNotFoundException(portfolioId);
        }

        List<Transaction> transactions =
                transactionRepository
                        .findByPortfolioIdOrderByTransactionDateAscIdAsc(
                                portfolioId
                        );

        Map<Long, HoldingState> states = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {

            Asset asset = transaction.getAsset();

            HoldingState state = states.computeIfAbsent(
                    asset.getId(),
                    id -> new HoldingState(asset)
            );

            if (transaction.getType() == TransactionType.BUY) {
                processBuy(state, transaction);
            } else {
                processSell(state, transaction);
            }
        }

        List<HoldingResponse> holdings = new ArrayList<>();

        for (HoldingState state : states.values()) {
            holdings.add(toResponse(state));
        }

        return holdings;
    }

    private void processBuy(
            HoldingState state,
            Transaction transaction
    ) {
        BigDecimal existingCostBasis =
                state.averageCost.multiply(state.quantity);

        BigDecimal purchaseCost =
                transaction.getPrice()
                        .multiply(transaction.getQuantity());

        BigDecimal newQuantity =
                state.quantity.add(transaction.getQuantity());

        BigDecimal newCostBasis =
                existingCostBasis.add(purchaseCost);

        state.averageCost = newCostBasis.divide(
                newQuantity,
                8,
                RoundingMode.HALF_UP
        );

        state.quantity = newQuantity;
    }

    private void processSell(
            HoldingState state,
            Transaction transaction
    ) {
        BigDecimal saleProfitPerUnit =
                transaction.getPrice()
                        .subtract(state.averageCost);

        BigDecimal realisedProfit =
                saleProfitPerUnit.multiply(
                        transaction.getQuantity()
                );

        state.realisedProfitLoss =
                state.realisedProfitLoss.add(realisedProfit);

        state.quantity =
                state.quantity.subtract(transaction.getQuantity());

        if (state.quantity.compareTo(BigDecimal.ZERO) == 0) {
            state.averageCost = BigDecimal.ZERO;
        }
    }

    private HoldingResponse toResponse(HoldingState state) {

        BigDecimal marketValue =
                state.quantity.multiply(state.asset.getCurrentPrice());

        BigDecimal remainingCostBasis =
                state.quantity.multiply(state.averageCost);

        BigDecimal unrealisedProfitLoss =
                marketValue.subtract(remainingCostBasis);

        return new HoldingResponse(
                state.asset.getId(),
                state.asset.getSymbol(),
                state.asset.getName(),
                state.quantity,
                state.averageCost,
                state.asset.getCurrentPrice(),
                marketValue,
                unrealisedProfitLoss,
                state.realisedProfitLoss
        );
    }

    private static class HoldingState {

        private final Asset asset;

        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal averageCost = BigDecimal.ZERO;
        private BigDecimal realisedProfitLoss = BigDecimal.ZERO;

        private HoldingState(Asset asset) {
            this.asset = asset;
        }
    }
}