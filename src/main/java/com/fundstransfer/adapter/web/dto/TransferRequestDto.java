package com.fundstransfer.adapter.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull String fromAccountId,
        @NotNull String toAccountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String description
) {
}