package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequestDTO;
import com.example.bankcards.dto.RegistrationRequestDTO;
import com.example.bankcards.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<String> register
            (@RequestBody @Valid RegistrationRequestDTO dto) {
        authServiceImpl.register(dto);
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login
            (@RequestBody @Valid LoginRequestDTO dto) {
        String token = authServiceImpl.login(dto);
        return ResponseEntity.ok(token);
    }
}
