package com.fundstransfer.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@With
public class Transfer {
    private String id;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private TransferStatus status;
    private LocalDateTime createdAt;
    private String description;
} 