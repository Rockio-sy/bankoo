package com.example.bankcards.service;


import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.ForbiddenRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CardServiceTest {

    @Mock CardRepository cardRepository;
    @Mock
    EncryptionUtil encryptionUtil;
    @Mock UserRepository userRepository;

    @InjectMocks
    CardServiceImpl cardService;

    UUID userId;
    UserDetailsImpl principal;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .fullName("fullNameTest")
                .username("testuser")
                .password("pass")
                .createdAt(LocalDateTime.now())
                .build();

        principal = new UserDetailsImpl(user);
        authentication = mock(Authentication.class);

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return CardDTO when user owns card")
    void testGetCardById_Success() {
        UUID cardId = UUID.randomUUID();
        User user = User.builder().id(userId).fullName("testuser").build();
        Card card = Card.builder()
                .id(cardId)
                .user(user)
                .cardNumber("encryptedNum")
                .expirationDate(LocalDate.now().plusYears(2))
                .cardStatus(CardStatus.ACTIVE)
                .balance(BigDecimal.TEN)
                .build();
        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(encryptionUtil.decrypt("encryptedNum")).thenReturn("1234567890123456");

        CardDTO dto = cardService.getCardById(cardId.toString());
        assertEquals(cardId, dto.id());
        assertEquals("testuser", dto.ownerName());
        assertTrue(dto.maskedNumber().startsWith("**** **** **** "));
    }

    @Test
    @DisplayName("Should throw NotFoundException if card does not exist")
    void testGetCardById_NotFound() {
        UUID cardId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.getCardById(cardId.toString()));
    }

    @Test
    @DisplayName("Should throw ForbiddenRequestException if user does not own card")
    void testGetCardById_Forbidden() {
        UUID cardId = UUID.randomUUID();

        User otherUser = User.builder().id(UUID.randomUUID()).fullName("other").build();
        Card card = Card.builder().id(cardId).user(otherUser).build();
        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(ForbiddenRequestException.class, () -> cardService.getCardById(cardId.toString()));
    }

    @Test
    @DisplayName("Should transfer funds successfully between two user cards")
    void testTransfer_Success() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encTo")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("50"))
                .build();

        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("40"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        when(encryptionUtil.decrypt("encFrom")).thenReturn("1111222233334444");
        when(encryptionUtil.decrypt("encTo")).thenReturn("5555666677778888");

        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        TransferResponseDTO result = cardService.transfer(dto);

        assertEquals("**** **** **** 4444", result.fromCardNumber());
        assertEquals("**** **** **** 8888", result.toCardNumber());
        assertEquals(new BigDecimal("40"), result.amount());
        assertNotNull(result.timestamp());
    }

    @Test
    @DisplayName("Should throw if transferring to same card")
    void testTransfer_SameCard() {
        UUID sameId = UUID.randomUUID();
        TransferRequestDTO dto = new TransferRequestDTO(sameId.toString(), sameId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> cardService.transfer(dto));
        assertTrue(ex.getMessage().contains("must be different"));
    }

    @Test
    @DisplayName("Should throw NotFoundException if source card does not exist")
    void testTransfer_SourceNotFound() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.transfer(dto));
    }

    @Test
    @DisplayName("Should throw NotFoundException if destination card does not exist")
    void testTransfer_DestinationNotFound() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();
        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.transfer(dto));
    }

    @Test
    @DisplayName("Should throw ForbiddenRequestException if user does not own both cards")
    void testTransfer_UserNotOwner() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(User.builder().id(UUID.randomUUID()).build()) // other user
                .cardNumber("encTo")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("50"))
                .build();

        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(ForbiddenRequestException.class, () -> cardService.transfer(dto));
    }

    @Test
    @DisplayName("Should throw if source card is not active")
    void testTransfer_SourceNotActive() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.BLOCKED)
                .balance(new BigDecimal("100"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encTo")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("50"))
                .build();

        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalStateException.class, () -> cardService.transfer(dto));
    }

    @Test
    @DisplayName("Should throw if destination card is not active")
    void testTransfer_DestinationNotActive() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encTo")
                .cardStatus(CardStatus.BLOCKED)
                .balance(new BigDecimal("50"))
                .build();

        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalStateException.class, () -> cardService.transfer(dto));
    }

    @Test
    @DisplayName("Should throw if not enough funds in source card")
    void testTransfer_InsufficientFunds() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encFrom")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("5"))
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .user(User.builder().id(userId).build())
                .cardNumber("encTo")
                .cardStatus(CardStatus.ACTIVE)
                .balance(new BigDecimal("50"))
                .build();

        TransferRequestDTO dto = new TransferRequestDTO(fromCardId.toString(), toCardId.toString(), new BigDecimal("10"));

        when(authentication.getPrincipal()).thenReturn(principal);
        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        assertThrows(IllegalStateException.class, () -> cardService.transfer(dto));
    }


    @Test
    @DisplayName("Should update card status successfully")
    void testUpdateCardStatus_Success() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder().id(cardId).cardStatus(CardStatus.ACTIVE).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        cardService.updateCardStatus(cardId.toString(), "BLOCKED");
        assertEquals(CardStatus.BLOCKED, card.getCardStatus());
    }

    @Test
    @DisplayName("Should throw NotFoundException if card not found when updating status")
    void testUpdateCardStatus_CardNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.updateCardStatus(cardId.toString(), "BLOCKED"));
    }

    @Test
    @DisplayName("Should throw ForbiddenRequestException if new status is null")
    void testUpdateCardStatus_NullStatus() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder().id(cardId).cardStatus(CardStatus.ACTIVE).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        Exception ex = assertThrows(ForbiddenRequestException.class, () -> cardService.updateCardStatus(cardId.toString(), null));
        assertTrue(ex.getMessage().contains("cannot be null"));
    }

    // Optional: Add test for invalid status string if CardStatus.fromString throws exception
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid card status string")
    void testUpdateCardStatus_InvalidStatus() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder().id(cardId).cardStatus(CardStatus.ACTIVE).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardStatus(cardId.toString(), "INVALID_STATUS"));
    }





    @Test
    @DisplayName("Should create a card for user as admin successfully")
    void testCreateCardAsAdmin_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).fullName("User X").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encryptedNum");
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> {
            Card card = i.getArgument(0);
            card.setId(UUID.randomUUID());
            return card;
        });
        when(encryptionUtil.decrypt(anyString())).thenReturn("1234567890123456");

        CardDTO result = cardService.createCardAsAdmin(userId.toString(), new BigDecimal("500"));
        assertEquals("User X", result.ownerName());
        assertEquals("**** **** **** 3456", result.maskedNumber());
        assertEquals(new BigDecimal("500"), result.balance());
    }

    @Test
    @DisplayName("Should throw NotFoundException if user not found when creating card as admin")
    void testCreateCardAsAdmin_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.createCardAsAdmin(userId.toString(), BigDecimal.TEN));
    }

}
