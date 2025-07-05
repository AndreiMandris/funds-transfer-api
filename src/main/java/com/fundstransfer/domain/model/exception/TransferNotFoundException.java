package com.fundstransfer.domain.model.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(String message) {
        super(message);
    }

    public TransferNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 