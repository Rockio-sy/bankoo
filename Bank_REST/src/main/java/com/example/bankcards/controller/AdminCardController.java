package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardPageDTO;
import com.example.bankcards.dto.CreateCardRequestDTO;
import com.example.bankcards.service.CardService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    @GetMapping("/all")
    public ResponseEntity<CardPageDTO> getAllCards(
            @RequestParam(value = "status", required = false) String cardStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        CardPageDTO results = cardService.listAllCards(cardStatus, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<CardPageDTO> getAllForUser(
            @PathVariable @NonNull String userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size
    ) {
        CardPageDTO result = cardService.getCardsByUserId(userId, status, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCardByIdAsAdmin
            (@PathVariable String cardId) {
        CardDTO result = cardService.getCardByIdAsAdmin(cardId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/new")
    public ResponseEntity<CardDTO> createCard
            (@RequestBody CreateCardRequestDTO request) {
        CardDTO result = cardService.createCardAsAdmin(
                request.stringUserId(),
                request.initialBalance()
        );
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<String> ChangeCardStatus(
            @PathVariable @NonNull String cardId,
            @RequestParam @NonNull String status
    ) {
        cardService.updateCardStatus(cardId, status);
        return ResponseEntity.ok("Updated successfully");
    }

    @DeleteMapping("/{cardId}/delete")
    public ResponseEntity<String> deleteCard
            (@PathVariable @NonNull String cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card deleted successfully");
    }

}
