package com.example.bankcards.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    @Getter
    private String value;


    public static Role fromString(String value){
        for(Role role: Role.values()){
            if(role.getValue().equalsIgnoreCase(value)){
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

}

