package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardPageDTO;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AdminCardController.class})
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private com.example.bankcards.security.JwtUtil jwtUtil;

    @MockitoBean
    private com.example.bankcards.security.JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /api/v1/admin/cards/all as ADMIN returns cards page")
    void getAllCards_AsAdmin() throws Exception {
        CardDTO c = new CardDTO(UUID.randomUUID(), "Owner", "****1111", LocalDate.now(), null, BigDecimal.TEN);
        CardPageDTO page = new CardPageDTO(List.of(c), 0, 1, 1, 1);
        when(cardService.listAllCards(anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/cards/all")
                        .with(user("admin").roles("ADMIN"))
                        .param("status", "ACTIVE").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("****1111"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/cards/all/{userId} as ADMIN returns user cards")
    void getAllForUser_AsAdmin() throws Exception {
        String uid = UUID.randomUUID().toString();
        CardDTO c = new CardDTO(UUID.fromString(uid), "Owner", "****2222", LocalDate.now(), null, BigDecimal.ZERO);
        CardPageDTO page = new CardPageDTO(List.of(c), 0, 1, 1, 1);
        when(cardService.getCardsByUserId(eq(uid), anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/admin/cards/all/{userId}", uid)
                        .with(user("admin").roles("ADMIN"))
                        .param("status", "NEW").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("****2222"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/cards/{cardId} as ADMIN returns card")
    void getCardByIdAsAdmin_AsAdmin() throws Exception {
        String cid = "card123";
        CardDTO dto = new CardDTO(UUID.randomUUID(), "Owner", "****3333", LocalDate.now(), null, BigDecimal.ONE);
        when(cardService.getCardByIdAsAdmin(cid)).thenReturn(dto);
        mockMvc.perform(get("/api/v1/admin/cards/{cardId}", cid)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("****3333"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/cards/new as ADMIN returns created card")
    void createCard_AsAdmin() throws Exception {
        String uid = UUID.randomUUID().toString();
        CardDTO dto = new CardDTO(UUID.randomUUID(), "Owner", "****4444", LocalDate.now(), null, BigDecimal.valueOf(100));
        when(cardService.createCardAsAdmin(eq(uid), eq(BigDecimal.valueOf(100)))).thenReturn(dto);
        String json = String.format("{\"stringUserId\":\"%s\",\"initialBalance\":100}", uid);
        mockMvc.perform(post("/api/v1/admin/cards/new")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("****4444"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/cards/{cardId}/status as ADMIN returns success")
    void changeCardStatus_AsAdmin() throws Exception {
        String cid = "card123";
        doNothing().when(cardService).updateCardStatus(cid, "BLOCKED");
        mockMvc.perform(patch("/api/v1/admin/cards/{cardId}/status", cid)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/cards/{cardId}/delete as ADMIN returns success")
    void deleteCard_AsAdmin() throws Exception {
        String cid = "card123";
        doNothing().when(cardService).deleteCard(cid);
        mockMvc.perform(delete("/api/v1/admin/cards/{cardId}/delete", cid)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Card deleted successfully"));
    }




}
