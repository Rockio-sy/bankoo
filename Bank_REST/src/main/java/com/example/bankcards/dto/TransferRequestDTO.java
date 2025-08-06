package com.example.bankcards.dto;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;

import java.math.BigDecimal;

public record TransferRequestDTO(
        @NonNull String fromCard,
        @NonNull String toCard,
        @NonNull @Positive BigDecimal amount
) {
}
