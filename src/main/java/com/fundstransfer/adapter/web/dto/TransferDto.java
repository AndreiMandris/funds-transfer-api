package com.fundstransfer.adapter.web.dto;

import com.fundstransfer.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferDto(
        String id,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount,
        TransferStatus status,
        LocalDateTime createdAt,
        String description
) {
} 