package com.aurum.exception;

public class DuplicateAssetSymbolException extends RuntimeException {

    public DuplicateAssetSymbolException(String symbol) {
        super("Asset already exists with symbol: " + symbol);
    }
}