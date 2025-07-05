package com.fundstransfer.domain.model.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}