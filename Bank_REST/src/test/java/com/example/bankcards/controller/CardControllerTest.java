package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /api/v1/cards/all returns page of cards")
    void listCurrentUserCards_ShouldReturnPage() throws Exception {
        CardDTO dto = new CardDTO(UUID.randomUUID(), "Owner", "****1234", LocalDate.now(), null, BigDecimal.valueOf(100));
        CardPageDTO page = CardPageDTO.builder()
                .content(List.of(dto))
                .page(0).size(1).totalElements(1).totalPages(1)
                .build();
        when(cardService.listAllCardForCurrentUser(anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/cards/all")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ownerName").value("Owner"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/cards/raw/{cardId} returns raw card")
    void getCardByIdWithoutMasking_ShouldReturnDto() throws Exception {
        UUID id = UUID.randomUUID();
        CardDTO dto = new CardDTO(id, "Owner", "4111222233334444", LocalDate.now(), null, BigDecimal.ZERO);
        when(cardService.getCardByIdWithoutMasking(id.toString())).thenReturn(dto);
        mockMvc.perform(get("/api/v1/cards/raw/{cardId}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("4111222233334444"));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{cardId} returns masked card")
    void getCardById_ShouldReturnDto() throws Exception {
        UUID id = UUID.randomUUID();
        CardDTO dto = new CardDTO(id, "Owner", "****4444", LocalDate.now(), null, BigDecimal.ZERO);
        when(cardService.getCardById(id.toString())).thenReturn(dto);
        mockMvc.perform(get("/api/v1/cards/{cardId}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("****4444"));
    }

    @Test
    @DisplayName("POST /api/v1/cards/block-request/{cardId} returns 200")
    void requestCardBlock_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).requestBlockCard(anyString());
        mockMvc.perform(post("/api/v1/cards/block-request/{cardId}", "card123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/cards/transfers returns response DTO")
    void transfer_ShouldReturnTransferResponse() throws Exception {
        TransferResponseDTO resp = new TransferResponseDTO("1234", "5678", BigDecimal.valueOf(50), LocalDateTime.now());
        when(cardService.transfer(any(TransferRequestDTO.class))).thenReturn(resp);
        String json = """
                {
                  "fromCard": "1234",
                  "toCard": "5678",
                  "amount": 50
                }
                """;
        mockMvc.perform(post("/api/v1/cards/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCardNumber").value("1234"))
                .andExpect(jsonPath("$.amount").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{cardId}/balance returns balance string")
    void checkBalance_ShouldReturnBalance() throws Exception {
        when(cardService.checkBalance(anyString())).thenReturn("100.00");
        mockMvc.perform(get("/api/v1/cards/{cardId}/balance", "card123"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));
    }
}
