package com.example.bankcards.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserPageDTO(
        List<UserDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
