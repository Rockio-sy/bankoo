package com.example.bankcards.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CardPageDTO(
        List<CardDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
