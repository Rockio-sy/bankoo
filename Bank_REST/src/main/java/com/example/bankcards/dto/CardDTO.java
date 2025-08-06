package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardDTO(
        UUID id,
        String ownerName,
        String maskedNumber,
        LocalDate expirationDate,
        CardStatus cardStatus,
        BigDecimal balance) {
}
