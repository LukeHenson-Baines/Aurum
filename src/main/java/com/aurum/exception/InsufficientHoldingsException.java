package com.aurum.exception;

import java.math.BigDecimal;

public class InsufficientHoldingsException extends RuntimeException {

    public InsufficientHoldingsException(
            String symbol,
            BigDecimal available,
            BigDecimal requested
    ) {
        super(
                "Insufficient holdings for " + symbol +
                ": available " + available +
                ", requested " + requested
        );
    }
}