package com.fundstransfer.domain.model.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExchangeRateException extends RuntimeException {

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
} 