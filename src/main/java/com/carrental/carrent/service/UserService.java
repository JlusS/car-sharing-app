package com.carrental.carrent.service;

import com.carrental.carrent.dto.user.UserRegistrationRequestDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.dto.user.UserRoleUpdateDto;
import com.carrental.carrent.dto.user.UserUpdateRequestDto;
import com.carrental.carrent.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateRole(Long id, UserRoleUpdateDto roleUpdateDto);

    UserResponseDto showUser();

    UserResponseDto findById(Long id);

    UserResponseDto updateUser(UserUpdateRequestDto requestDto);
}
