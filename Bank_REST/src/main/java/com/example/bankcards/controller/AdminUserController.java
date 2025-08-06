package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreationRequestDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserPageDTO;
import com.example.bankcards.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;


    @GetMapping("/all")
    public ResponseEntity<UserPageDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fullName) {
        UserPageDTO results = userService.getAllUsers(page, size, fullName);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @NonNull String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable @NonNull String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser
            (UserCreationRequestDTO dto){
        UserDTO result = userService.createUserByAdmin(dto);

        return ResponseEntity.ok(result);
    }
}
