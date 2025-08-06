package com.example.bankcards.service;


import com.example.bankcards.util.EncryptionUtilImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtilImpl encryptionService;


    private final String key = "1234567890123456";
    private final String iv = "abcdefghijklmnop";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionUtilImpl();


        setField(encryptionService, "secretKey", key);
        setField(encryptionService, "initVector", iv);


        encryptionService.init();
    }

    @Test
    @DisplayName("Should encrypt and decrypt string successfully")
    void testEncryptDecrypt() {
        String plain = "SensitiveData123!";

        String encrypted = encryptionService.encrypt(plain);
        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    @DisplayName("Should throw exception on decryption if ciphertext is invalid")
    void testDecryptInvalidCiphertext() {
        String invalidCipher = "notbase64==";
        RuntimeException ex = assertThrows(RuntimeException.class, () -> encryptionService.decrypt(invalidCipher));
        assertTrue(ex.getMessage().contains("Decryption failed"));
    }

    @Test
    @DisplayName("Should throw exception on encryption if algorithm is tampered")
    void testEncryptThrowsOnAlgorithmFailure() {
        // Tamper with keySpec to simulate error
        setField(encryptionService, "keySpec", null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> encryptionService.encrypt("test"));
        assertTrue(ex.getMessage().contains("Encryption failed"));
    }

    private static void setField(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
