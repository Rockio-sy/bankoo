package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID userId,
        String fullName,
        String username,
        Role role,
        LocalDateTime createdAt
) {
}
