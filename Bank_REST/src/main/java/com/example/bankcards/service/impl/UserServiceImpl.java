package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserCreationRequestDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserPageDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserPageDTO getAllUsers(int page, int size, String fullName) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> saved = (StringUtils.hasText(fullName))
                ? userRepository.findByFullNameContainingIgnoreCase(fullName, pageable)
                : userRepository.findAll(pageable);

        Page<UserDTO> mapped = saved.map(this::toDto);

        return UserPageDTO.builder()
                .content(mapped.getContent())
                .size(mapped.getSize())
                .page(mapped.getNumber())
                .totalElements(mapped.getTotalElements())
                .totalPages(mapped.getTotalPages())
                .build();
    }

    @Override
    public UserDTO getUserById(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        return toDto(user);
    }

    @Override
    public void deleteUser(String userId) {
        UUID id = UUID.fromString(userId);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO createUserByAdmin(UserCreationRequestDTO dto) {
        User user = User.builder()
                .username(dto.username())
                .fullName(dto.fullName())
                .role(Role.fromString(dto.role()))
                .password(passwordEncoder.encode(dto.password()))
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }



    private UserDTO toDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
