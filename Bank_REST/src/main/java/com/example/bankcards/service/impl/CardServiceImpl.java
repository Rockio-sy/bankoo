package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardPageDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserDetailsImpl;
import com.example.bankcards.exception.ForbiddenRequestException;
import com.example.bankcards.exception.InternalServerException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.EncryptionUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

    // User
    private final CardRepository cardRepository;
    private final EncryptionUtil encryptionUtil;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CardPageDTO listAllCardForCurrentUser
            (String cardStatus, int page, int size) {

        UUID userId = getUserIdFromSecurityContext();
        Pageable pageable = PageRequest.of(page, size);
        CardStatus status = cardStatus != null ? CardStatus.fromString(cardStatus) : null;

        Page<Card> cardsPage = (cardStatus != null)
                ? cardRepository.findByUserIdAndCardStatus(userId, status, pageable)
                : cardRepository.findByUserId(userId, pageable);

        Page<CardDTO> mappedPage = cardsPage.map(this::toDto);
        return CardPageDTO.builder()
                .content(mappedPage.getContent())
                .totalPages(mappedPage.getTotalPages())
                .totalElements(mappedPage.getTotalElements())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public CardDTO getCardById
            (String stringCardId) {
        UUID currentUserId = getUserIdFromSecurityContext();
        UUID cardId = UUID.fromString(stringCardId);
        Card saved = cardRepository.findById(cardId).orElseThrow(
                () -> new NotFoundException("Card not found"));

        if (!saved.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenRequestException("Current user doesn't own chosen card");
        }

        return toDto(saved);
    }

    @Override
    public CardDTO getCardByIdWithoutMasking
            (String stringCardId) {
        UUID currentUserId = getUserIdFromSecurityContext();
        UUID cardId = UUID.fromString(String.valueOf(stringCardId));

        Card saved = cardRepository.findById(cardId).orElseThrow(
                () -> new NotFoundException("Card not found"));

        if (!saved.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenRequestException("Current user doesn't own chosen card");
        }

        return toDtoWithoutMasking(saved);
    }

    @Override
    @Transactional
    public void requestBlockCard
            (String stringCardId) {
        UUID currentUserId = getUserIdFromSecurityContext();

        UUID cardId = UUID.fromString(stringCardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenRequestException("Current user doesn't own chosen card");
        }

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card is already blocked.");
        }

        if (card.getCardStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Card has already expired.");
        }

        card.setCardStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }


    @Override
    @Transactional
    public TransferResponseDTO transfer
            (TransferRequestDTO dto) {
        UUID currentUserId = getUserIdFromSecurityContext();
        UUID fromCardId = UUID.fromString((dto.fromCard()));
        UUID toCardId = UUID.fromString(dto.toCard());

        if (dto.fromCard().equals(dto.toCard())) {
            throw new IllegalArgumentException("Source and destination cards must be different.");
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Source card not found"));

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Destination card not found"));

        if (!fromCard.getUser().getId().equals(currentUserId) ||
                !toCard.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenRequestException("You do not own both cards.");
        }

        if (fromCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active.");
        }

        if (toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Destination card is not active.");
        }

        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds in source card.");
        }

        // Transfer
        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Masked card numbers
        String fromMasked = mask(encryptionUtil.decrypt(fromCard.getCardNumber()));
        String toMasked = mask(encryptionUtil.decrypt(toCard.getCardNumber()));

        return new TransferResponseDTO(fromMasked, toMasked, dto.amount(), LocalDateTime.now());
    }

    @Override
    public String checkBalance(@NonNull String stringCardId) {
        UUID cardId = UUID.fromString(stringCardId);

        Card saved = cardRepository.findById(cardId).orElseThrow
                (() -> new NotFoundException("Card not found with ID: " + stringCardId));

        return saved.getBalance().toString();
    }


    // Admin
    @Override
    @Transactional(readOnly = true)
    public CardPageDTO listAllCards
    (String cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        CardStatus status = cardStatus != null ? CardStatus.fromString(cardStatus) : null;

        Page<Card> cardsPage = (status != null)
                ? cardRepository.findByCardStatus(status, pageable)
                : cardRepository.findAll(pageable);

        Page<CardDTO> mapped = cardsPage.map(this::toDto);

        return CardPageDTO.builder()
                .content(mapped.getContent())
                .size(mapped.getSize())
                .page(mapped.getNumber())
                .totalElements(mapped.getTotalElements())
                .totalPages(mapped.getTotalPages())
                .build();
    }

    @Override
    public CardPageDTO getCardsByUserId
            (String stringUserId, String cardStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        CardStatus status = cardStatus != null
                ? CardStatus.fromString(cardStatus)
                : null;

        UUID userId = UUID.fromString(stringUserId);
        Page<Card> cards = (status != null)
                ? cardRepository.findByUserIdAndCardStatus(userId, status, pageable)
                : cardRepository.findByUserId(userId, pageable);

        Page<CardDTO> mapped = cards.map(this::toDto);
        return CardPageDTO.builder()
                .totalPages(mapped.getTotalPages())
                .page(mapped.getNumber())
                .totalElements(mapped.getTotalElements())
                .size(mapped.getSize())
                .content(mapped.getContent())
                .build();
    }

    @Override
    public CardDTO getCardByIdAsAdmin
            (String stringCardId) {
        Card saved = cardRepository.findById(UUID.fromString(stringCardId))
                .orElseThrow(() -> new NotFoundException("Card not found with id: " + stringCardId));
        return toDto(saved);
    }


    @Override
    @Transactional
    public CardDTO createCardAsAdmin
            (String stringUserId, BigDecimal initialBalance) {
        UUID userId = UUID.fromString(stringUserId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found with ID: " + stringUserId));

        String rawNumber = generateCardNumber();
        String encryptionNumber = encryptionUtil.encrypt(rawNumber);

        Card card = Card.builder()
                .cardNumber(encryptionNumber)
                .cardStatus(CardStatus.ACTIVE)
                .user(user)
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(initialBalance)
                .build();

        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void updateCardStatus
            (String stringCardId, String newStatus) {
        Card saved = cardRepository.findById(UUID.fromString(stringCardId)).orElseThrow(
                () -> new NotFoundException("Card not found with ID: " + stringCardId)
        );

        if (newStatus == null) {
            throw new ForbiddenRequestException("New status cannot be null, allowed status [ACTIVE, BLOCKED, EXPIRED]");
        }
        CardStatus status = CardStatus.fromString(newStatus);

        saved.setCardStatus(status);
        cardRepository.save(saved);
    }


    @Override
    public void deleteCard
            (String stringCardId) {
        Card saved = cardRepository.findById(UUID.fromString(stringCardId)).orElseThrow(
                () -> new NotFoundException("Card not found with ID " + stringCardId)
        );

        cardRepository.delete(saved);
    }


    private String mask
            (String rawNumber) {
        String last4 = rawNumber.length() >= 4
                ? rawNumber.substring(rawNumber.length() - 4)
                : rawNumber;
        return "**** **** **** " + last4;
    }


    private UUID getUserIdFromSecurityContext
            () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    private CardDTO toDto
            (Card card) {
        String rawNumber = encryptionUtil.decrypt(card.getCardNumber());

        String masked = mask(rawNumber);

        return new CardDTO(
                card.getId(),
                card.getUser().getFullName(),
                masked,
                card.getExpirationDate(),
                card.getCardStatus(),
                card.getBalance()
        );
    }

    private CardDTO toDtoWithoutMasking(Card card) {
        String raw = encryptionUtil.decrypt(card.getCardNumber());
        return new CardDTO(
                card.getId(),
                card.getUser().getFullName(),
                raw,
                card.getExpirationDate(),
                card.getCardStatus(),
                card.getBalance()
        );
    }

    // Number generation algorithm
    private int calculateLuhnChecksum(int[] digits) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = digits.length - 1; i >= 0; i--) {
            int d = digits[i];
            if (doubleDigit) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }

        int mod = sum % 10;
        return (mod == 0) ? 0 : 10 - mod;
    }


    private String generateLuhnCardNumber() {
        Random random = new Random();
        int[] digits = new int[15];

        // Generate first 15 digits randomly
        for (int i = 0; i < 15; i++) {
            digits[i] = random.nextInt(10);
        }

        // Calculate checksum (16th digit)
        int checksum = calculateLuhnChecksum(digits);

        // Build final card number
        StringBuilder builder = new StringBuilder();
        for (int digit : digits) {
            builder.append(digit);
        }
        builder.append(checksum); // 16th digit

        return builder.toString();
    }

    private String generateCardNumber() {
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            String raw = generateLuhnCardNumber();
            String encrypted = encryptionUtil.encrypt(raw);
            if (!cardRepository.existsByCardNumber(encrypted)) {
                return raw;
            }
        }
        throw new InternalServerException("Failed to generate unique card number after retries");
    }


}
