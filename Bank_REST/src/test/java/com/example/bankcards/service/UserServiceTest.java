package com.example.bankcards.service;


import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserPageDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    UUID userId;
    User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .fullName("John Doe")
                .username("johnny")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return paginated users without fullName filter")
    void testGetAllUsers_NoFilter() {
        Page<User> usersPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(usersPage);

        UserPageDTO result = userService.getAllUsers(0, 10, null);

        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        assertEquals("John Doe", result.content().get(0).fullName());
    }

    @Test
    @DisplayName("Should return paginated users with fullName filter")
    void testGetAllUsers_WithFilter() {
        Page<User> usersPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findByFullNameContainingIgnoreCase(eq("John"), any(Pageable.class)))
                .thenReturn(usersPage);

        UserPageDTO result = userService.getAllUsers(0, 10, "John");

        assertEquals(1, result.totalElements());
        assertEquals("John Doe", result.content().get(0).fullName());
    }

    @Test
    @DisplayName("Should return UserDTO when user exists")
    void testGetUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO dto = userService.getUserById(userId.toString());

        assertEquals(userId, dto.userId());
        assertEquals("John Doe", dto.fullName());
        assertEquals("johnny", dto.username());
        assertEquals(Role.USER, dto.role());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found")
    void testGetUserById_NotFound() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Should delete user when exists")
    void testDeleteUser_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.deleteUser(userId.toString()));
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw NotFoundException if user does not exist on delete")
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId.toString()));
        verify(userRepository, never()).deleteById(any());
    }
}
