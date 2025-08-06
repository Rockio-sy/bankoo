package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardPageDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import lombok.NonNull;

import java.math.BigDecimal;


public interface CardService {

    CardPageDTO listAllCards(String cardStatus, int page, int size);

    public CardDTO getCardByIdWithoutMasking(String stringCardId);

    CardPageDTO getCardsByUserId(String stringUserId, String cardStatus, int page, int size);

    CardDTO getCardByIdAsAdmin(String stringCardId);

    CardDTO createCardAsAdmin(String stringUserId, BigDecimal initialBalance);

    void updateCardStatus(String stringCardId, String newStatus);

    void deleteCard(String stringCardId);

    CardPageDTO listAllCardForCurrentUser(String cardStatus, int page, int size);

    CardDTO getCardById(String stringCardId);

    void requestBlockCard(String stringCardId);

    TransferResponseDTO transfer(TransferRequestDTO dto);

    String checkBalance(@NonNull String stringCardId);
}
