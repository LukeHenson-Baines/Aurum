package com.aurum.controller;

import com.aurum.dto.TransactionRequest;
import com.aurum.dto.TransactionResponse;
import com.aurum.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @PathVariable Long portfolioId,
            @Valid @RequestBody TransactionRequest request
    ) {
        return transactionService.createTransaction(portfolioId, request);
    }

    @GetMapping
    public List<TransactionResponse> getTransactionsForPortfolio(
            @PathVariable Long portfolioId
    ) {
        return transactionService.getTransactionsForPortfolio(portfolioId);
    }
}