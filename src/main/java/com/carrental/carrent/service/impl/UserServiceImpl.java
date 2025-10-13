package com.carrental.carrent.service.impl;

import com.carrental.carrent.dto.user.UserRegistrationRequestDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.dto.user.UserRoleUpdateDto;
import com.carrental.carrent.dto.user.UserUpdateRequestDto;
import com.carrental.carrent.exception.RegistrationException;
import com.carrental.carrent.mapper.UserMapper;
import com.carrental.carrent.model.Role;
import com.carrental.carrent.model.User;
import com.carrental.carrent.repository.user.UserRepository;
import com.carrental.carrent.security.AuthenticationService;
import com.carrental.carrent.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with this email: "
                    + requestDto.getEmail()
                    + " already exists");
        }

        User user = userMapper.toUserModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        System.out.println("Registered user: " + user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateRole(Long userId, UserRoleUpdateDto roleUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: "
                                + userId));
        user.setRole(roleUpdateDto.getRole());
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto showUser() {
        User authenticatedUser = authenticationService.getAuthenticatedUser();

        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: "
                        + authenticatedUser.getId()));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: "
                        + id));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(UserUpdateRequestDto requestDto) {
        User currentUser = authenticationService.getAuthenticatedUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userMapper.updateUserFromDto(requestDto, user);

        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);
    }
}
