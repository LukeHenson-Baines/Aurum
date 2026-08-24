package com.aurum.controller;

import com.aurum.dto.TransactionResponse;
import com.aurum.exception.InsufficientHoldingsException;
import com.aurum.model.TransactionType;
import com.aurum.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldReturn201() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L,
                1L,
                1L,
                "AAPL",
                TransactionType.BUY,
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 8, 24),
                LocalDateTime.of(2026, 8, 24, 14, 0)
        );

        when(transactionService.createTransaction(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/portfolios/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetId": 1,
                                  "type": "BUY",
                                  "quantity": 10,
                                  "price": 180.00,
                                  "transactionDate": "2026-08-24"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetSymbol").value("AAPL"))
                .andExpect(jsonPath("$.type").value("BUY"));
    }

    @Test
    void getTransactions_shouldReturn200() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L,
                1L,
                1L,
                "AAPL",
                TransactionType.BUY,
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 8, 24),
                LocalDateTime.of(2026, 8, 24, 14, 0)
        );

        when(transactionService.getTransactionsForPortfolio(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/portfolios/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetSymbol").value("AAPL"));
    }

    @Test
    void createTransaction_shouldReturn409WhenOverselling() throws Exception {
        when(transactionService.createTransaction(eq(1L), any()))
                .thenThrow(new InsufficientHoldingsException(
                        "AAPL",
                        new BigDecimal("6"),
                        new BigDecimal("7")
                ));

        mockMvc.perform(post("/api/portfolios/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetId": 1,
                                  "type": "SELL",
                                  "quantity": 7,
                                  "price": 215.00,
                                  "transactionDate": "2026-08-24"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient holdings for AAPL: available 6, requested 7"));
    }

    @Test
    void createTransaction_shouldReturn400ForZeroQuantity() throws Exception {
        mockMvc.perform(post("/api/portfolios/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetId": 1,
                                  "type": "BUY",
                                  "quantity": 0,
                                  "price": 180.00,
                                  "transactionDate": "2026-08-24"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity")
                        .value("Quantity must be greater than zero"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_shouldReturn400ForNegativePrice() throws Exception {
        mockMvc.perform(post("/api/portfolios/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetId": 1,
                                  "type": "BUY",
                                  "quantity": 10,
                                  "price": -1,
                                  "transactionDate": "2026-08-24"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price")
                        .value("Price must be greater than zero"));

        verifyNoInteractions(transactionService);
    }
}