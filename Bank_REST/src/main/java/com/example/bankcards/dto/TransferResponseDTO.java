package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponseDTO(
        String fromCardNumber,
        String toCardNumber,
        BigDecimal amount,
        LocalDateTime timestamp
) {
}
