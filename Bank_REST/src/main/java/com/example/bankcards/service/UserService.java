package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreationRequestDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserPageDTO;


public interface UserService {
    UserPageDTO getAllUsers(int page, int size, String fullName);

    UserDTO getUserById(String userId);

    UserDTO createUserByAdmin(UserCreationRequestDTO dto);

    void deleteUser(String userId);
}
