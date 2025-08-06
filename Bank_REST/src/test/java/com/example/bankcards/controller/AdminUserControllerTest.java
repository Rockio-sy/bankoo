package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserPageDTO;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AdminUserController.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.example.bankcards.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.bankcards.security.JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /api/v1/admin/users/all returns page of users for ADMIN")
    void getAllUsers_ShouldReturnPage() throws Exception {
        UserDTO u = new UserDTO(java.util.UUID.randomUUID(), "Alice", "aliceUser", null, LocalDateTime.now());
        UserPageDTO page = new UserPageDTO(List.of(u), 0, 1, 1, 1);
        when(userService.getAllUsers(anyInt(), anyInt(), anyString())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/users/all")
                        .with(user("admin").roles("ADMIN"))
                        .param("page", "0").param("size", "1").param("fullName", "Alice"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} returns user for ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        UserDTO u = new UserDTO(java.util.UUID.randomUUID(), "Bob", "bobUser", null, LocalDateTime.now());
        when(userService.getUserById("id2")).thenReturn(u);
        mockMvc.perform(get("/api/v1/admin/users/{userId}", "id2")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/users/{userId} returns success for ADMIN")
    void deleteUser_ShouldReturnSuccess() throws Exception {
        doNothing().when(userService).deleteUser("id3");
        mockMvc.perform(delete("/api/v1/admin/users/{userId}", "id3")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }


}
