package com.example.bankcards.service.impl;

import com.example.bankcards.dto.LoginRequestDTO;
import com.example.bankcards.dto.RegistrationRequestDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserDetailsImpl;
import com.example.bankcards.exception.InternalServerException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public void register
            (RegistrationRequestDTO dto) {
        if (userRepository.existsByUsername(dto.userName())) {
            throw new BadCredentialsException("Username already exists");
        }

        User user = User.builder()
                .fullName(dto.fullName())
                .username(dto.userName())
                .role(Role.USER)
                .password(passwordEncoder.encode(dto.password()))
                .build();
        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            throw new InternalServerException("Internal server error", e);
        }
    }

    @Override
    public String login
            (LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.userName(), dto.password())
        );
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        return jwtUtil.generateToken(principal.getId(), principal.getUsername(), principal.getRole());
    }



}
