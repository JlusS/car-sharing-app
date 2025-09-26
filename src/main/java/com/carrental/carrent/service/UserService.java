package com.carrental.carrent.service;

import com.carrental.carrent.dto.user.UserRegistrationRequestDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;
}
