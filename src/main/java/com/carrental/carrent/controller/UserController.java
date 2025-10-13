package com.carrental.carrent.controller;

import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.dto.user.UserRoleUpdateDto;
import com.carrental.carrent.dto.user.UserUpdateRequestDto;
import com.carrental.carrent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role", description =
            "Update the role of a user. Accessible by MANAGER role.")
    public UserResponseDto updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateDto role) {
        return userService.updateRole(userId, role);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @GetMapping("/me")
    @Operation(summary = "Get user by ID", description =
            "Returns a user by their ID. Accessible by MANAGER and CUSTOMER roles.")
    public UserResponseDto getUserById() {
        return userService.showUser();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @PutMapping("/me")
    @Operation(summary = "Update user information", description =
            "Update user information. Accessible by MANAGER and CUSTOMER roles.")
    public UserResponseDto updateUser(
            @RequestBody @Valid
            UserUpdateRequestDto requestDto) {
        return userService.updateUser(requestDto);
    }
}
