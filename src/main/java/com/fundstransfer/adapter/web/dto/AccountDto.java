package com.fundstransfer.adapter.web.dto;

import java.math.BigDecimal;

public record AccountDto(
        String id,
        Long ownerId,
        String currency,
        BigDecimal balance
) {
} 