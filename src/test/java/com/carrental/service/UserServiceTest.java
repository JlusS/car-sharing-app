package com.carrental.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrental.dto.user.UserRegistrationRequestDto;
import com.carrental.dto.user.UserResponseDto;
import com.carrental.dto.user.UserRoleUpdateDto;
import com.carrental.dto.user.UserUpdateRequestDto;
import com.carrental.exception.RegistrationException;
import com.carrental.mapper.UserMapper;
import com.carrental.model.Role;
import com.carrental.model.User;
import com.carrental.repository.user.UserRepository;
import com.carrental.security.AuthenticationService;
import com.carrental.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register user - should register and return user response DTO")
    void register_ValidRequest_ShouldRegisterUser() throws RegistrationException {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(Role.CUSTOMER);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("test@example.com");
        responseDto.setFirstName("John");
        responseDto.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toUserModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.register(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userMapper).toUserModel(requestDto);
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Register user - should throw exception when email already exists")
    void register_ExistingEmail_ShouldThrowException() {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
        assertEquals("User with this email: existing@example.com already exists",
                exception.getMessage());
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    @DisplayName("Update role - should update user role and return response DTO")
    void updateRole_ValidRequest_ShouldUpdateRole() {
        // Given
        Long userId = 1L;
        UserRoleUpdateDto roleUpdateDto = new UserRoleUpdateDto();
        roleUpdateDto.setRole(Role.MANAGER);

        User user = new User();
        user.setId(userId);
        user.setRole(Role.CUSTOMER);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setRole(Role.MANAGER);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.updateRole(userId, roleUpdateDto);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Update role - should throw exception when user not found")
    void updateRole_NonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        UserRoleUpdateDto roleUpdateDto = new UserRoleUpdateDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateRole(userId, roleUpdateDto));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Show user - should return authenticated user response DTO")
    void showUser_AuthenticatedUser_ShouldReturnUserDto() {
        // Given
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("test@example.com");
        responseDto.setFirstName("John");
        responseDto.setLastName("Doe");

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.showUser();

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(authenticationService).getAuthenticatedUser();
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Find user by ID - should return user response DTO")
    void findById_ExistingUser_ShouldReturnUserDto() {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setEmail("test@example.com");
        responseDto.setFirstName("John");
        responseDto.setLastName("Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Update user - should update user details and return response DTO")
    void updateUser_ValidRequest_ShouldUpdateUser() {
        // Given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setFirstName("Updated");
        requestDto.setLastName("Name");
        requestDto.setPassword("newPassword");

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setFirstName("Original");
        user.setLastName("Name");
        user.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setPassword("encodedNewPassword");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setFirstName("Updated");
        responseDto.setLastName("Name");

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.updateUser(requestDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(authenticationService).getAuthenticatedUser();
        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDto(eq(requestDto), any(User.class));
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Update user without password - should update only name fields")
    void updateUser_WithoutPassword_ShouldUpdateOnlyNames() {
        // Given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setFirstName("Updated");
        requestDto.setLastName("Name");
        // No password provided

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setFirstName("Original");
        user.setLastName("Name");
        user.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setPassword("oldPassword");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setFirstName("Updated");
        responseDto.setLastName("Name");

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(responseDto);

        // When
        UserResponseDto result = userService.updateUser(requestDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(authenticationService).getAuthenticatedUser();
        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDto(eq(requestDto), any(User.class));
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponseDto(any(User.class));
    }

    @Test
    @DisplayName("Find user by ID - should throw exception when user not found")
    void findById_NonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findById(userId));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Show user - should throw exception when authenticated user not found")
    void showUser_AuthenticatedUserNotFound_ShouldThrowException() {
        // Given
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.showUser());
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(authenticationService).getAuthenticatedUser();
        verify(userRepository).findById(1L);
    }
}
