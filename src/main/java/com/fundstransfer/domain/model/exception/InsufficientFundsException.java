package com.fundstransfer.domain.model.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

}