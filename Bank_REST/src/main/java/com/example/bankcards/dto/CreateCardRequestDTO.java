package com.example.bankcards.dto;

import java.math.BigDecimal;

public record CreateCardRequestDTO(
        String stringUserId,
        BigDecimal initialBalance
) {
}
