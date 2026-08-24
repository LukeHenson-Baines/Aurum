package com.aurum.repository;

import com.aurum.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByPortfolioIdOrderByTransactionDateAscIdAsc(Long portfolioId);

    List<Transaction> findByPortfolioIdAndAssetIdOrderByTransactionDateAsc(
            Long portfolioId,
            Long assetId
    );

    List<Transaction> findByPortfolioIdOrderByTransactionDateAscIdAscIdAsc(
        Long portfolioId
    );
}