package com.example.bankcards.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public enum CardStatus {
    ACTIVE("active"),
    BLOCKED("blocked"),
    EXPIRED("expired");
    @Getter
    private String value;

    public static CardStatus fromString(String value) {
        for (CardStatus status: CardStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

}
