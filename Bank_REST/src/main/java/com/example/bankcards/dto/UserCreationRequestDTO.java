package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreationRequestDTO(
        @NotBlank String username,
        @NotBlank String fullName,
        @NotBlank String role,
        @NotBlank String password
) {
}
