package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationRequestDTO(
        @NotBlank String fullName,
        @NotBlank String userName,
        @NotBlank String password
) {
}
