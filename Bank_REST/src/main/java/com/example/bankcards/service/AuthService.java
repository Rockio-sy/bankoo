package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequestDTO;
import com.example.bankcards.dto.RegistrationRequestDTO;

public interface AuthService {
    void register(RegistrationRequestDTO dto);
    String login(LoginRequestDTO dto);
}
