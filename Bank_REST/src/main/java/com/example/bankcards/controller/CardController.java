package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardPageDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.service.CardService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping("/all")
    public ResponseEntity<CardPageDTO> listCurrentUserCards
            (@RequestParam(value = "status", required = false) String cardStatus,
             @RequestParam(value = "page", defaultValue = "0") @Min(value = 0) int page,
             @RequestParam(value = "size", defaultValue = "10") @Positive int size
            ) {
        CardPageDTO results = cardService.listAllCardForCurrentUser(cardStatus, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/raw/{cardId}")
    public ResponseEntity<CardDTO> getCardByIdWithoutMasking
            (@PathVariable @NonNull String cardId) {
        CardDTO result = cardService.getCardByIdWithoutMasking(cardId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCardById
            (@PathVariable @NonNull String cardId) {
        CardDTO result = cardService.getCardById(cardId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/block-request/{cardId}")
    public ResponseEntity<Void> requestCardBlock(@PathVariable String cardId) {
        cardService.requestBlockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDTO> transfer(@RequestBody TransferRequestDTO dto) {
        TransferResponseDTO result = cardService.transfer(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("{cardId}/balance")
    public ResponseEntity<String> checkBalance
            (@PathVariable @NonNull String cardId){
        String balance = cardService.checkBalance(cardId);
        return ResponseEntity.ok(balance);
    }

}
