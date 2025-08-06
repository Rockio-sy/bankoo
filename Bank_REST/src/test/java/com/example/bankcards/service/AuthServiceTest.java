package com.example.bankcards.service;


import com.example.bankcards.dto.LoginRequestDTO;
import com.example.bankcards.dto.RegistrationRequestDTO;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserDetailsImpl;
import com.example.bankcards.exception.InternalServerException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistrationRequestDTO registrationDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = new RegistrationRequestDTO("Test User", "testuser", "password123");
    }

    @Test
    @DisplayName("Should register user successfully with unique username")
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> authService.register(registrationDTO));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException if username exists")
    void testRegister_UsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.register(registrationDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InternalServerException on DataAccessException")
    void testRegister_InternalServerError() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("DB error") {});

        assertThrows(InternalServerException.class, () -> authService.register(registrationDTO));
    }

    // Add inside AuthServiceImplTest class

    @Test
    @DisplayName("Should return JWT token on successful login")
    void testLogin_Success() {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO("testUser", "pass");

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .role(Role.USER)
                .fullName("fullNameTest")
                .username("testUser")
                .password("pass")
                .createdAt(LocalDateTime.now())
                .build();
        // Prepare the mocked Authentication and UserDetailsImpl
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(id, "testUser", Role.USER)).thenReturn("mocked-jwt-token");

        // Act
        String token = authService.login(loginDTO);

        // Assert
        assertEquals("mocked-jwt-token", token);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid credentials")
    void testLogin_BadCredentials() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("testuser", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
    }

}

