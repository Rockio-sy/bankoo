package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequestDTO;
import com.example.bankcards.dto.RegistrationRequestDTO;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authServiceImpl;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/v1/auth/register returns 200 with success message")
    void register_ShouldReturn200AndMessage() throws Exception {
        doNothing().when(authServiceImpl).register(new RegistrationRequestDTO("Full Name", "username", "secret"));
        String json = """
            {
              "fullName": "Full Name",
              "userName": "username",
              "password": "secret"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Account created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 with JWT token")
    void login_ShouldReturn200AndToken() throws Exception {
        String dummyToken = "mocked.jwt.token";
        when(authServiceImpl.login(any(LoginRequestDTO.class))).thenReturn(dummyToken);
        String json = """
            {
              "userName": "username",
              "password": "secret"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(dummyToken));
    }
}
