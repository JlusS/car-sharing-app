package com.carrental.service;

import com.carrental.dto.user.UserRegistrationRequestDto;
import com.carrental.dto.user.UserResponseDto;
import com.carrental.dto.user.UserRoleUpdateDto;
import com.carrental.dto.user.UserUpdateRequestDto;
import com.carrental.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateRole(Long id, UserRoleUpdateDto roleUpdateDto);

    UserResponseDto showUser();

    UserResponseDto findById(Long id);

    UserResponseDto updateUser(UserUpdateRequestDto requestDto);
}
