package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findByUserId(UUID userId, Pageable pageable);
    Page<Card> findByUserIdAndCardStatus(UUID userId, CardStatus cardStatus, Pageable pageable);

    Page<Card> findByCardStatus(CardStatus status, Pageable pageable);

    boolean existsByCardNumber(String encrypted);
}
