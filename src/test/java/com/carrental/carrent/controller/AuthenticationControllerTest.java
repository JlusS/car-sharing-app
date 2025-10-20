package com.carrental.carrent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carrental.carrent.config.TestConfig;
import com.carrental.carrent.dto.user.UserLoginRequestDto;
import com.carrental.carrent.dto.user.UserLoginResponseDto;
import com.carrental.carrent.dto.user.UserRegistrationRequestDto;
import com.carrental.carrent.dto.user.UserResponseDto;
import com.carrental.carrent.model.Role;
import com.carrental.carrent.model.User;
import com.carrental.carrent.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@Sql(scripts = {
        "classpath:database/delete-data-from-tables.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/delete-data-from-tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthenticationControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(applicationContext)
                    .apply(springSecurity())
                    .build();
        }
    }

    @Test
    @DisplayName("Register new user - should create user successfully")
    void register_ValidRequestDto_ShouldCreateUser() throws Exception {
        // Given
        final long initialCount = userRepository.count();
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Password123");
        requestDto.setRepeatPassword("Password123");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getEmail(), actual.getEmail());
        assertEquals(requestDto.getFirstName(), actual.getFirstName());
        assertEquals(requestDto.getLastName(), actual.getLastName());
        assertEquals(initialCount + 1, userRepository.count());
    }

    @Test
    @DisplayName("Login user - should return token")
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setFirstName("Login");
        user.setLastName("User");
        user.setRole(com.carrental.carrent.model.Role.CUSTOMER);
        userRepository.save(user);

        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("login@example.com");
        requestDto.setPassword("Password123");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserLoginResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserLoginResponseDto.class
        );
        assertNotNull(response);
    }

    @Test
    @DisplayName("Register user with existing email - should return bad request")
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("Password123"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(com.carrental.carrent.model.Role.CUSTOMER);
        userRepository.save(existingUser);

        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("existing@example.com");
        requestDto.setPassword("Password123");
        requestDto.setRepeatPassword("Password123");
        requestDto.setFirstName("New");
        requestDto.setLastName("User");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register user with invalid data - should return bad request")
    void register_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("invalid-email");
        requestDto.setPassword("short");
        requestDto.setRepeatPassword("different");
        requestDto.setFirstName("");
        requestDto.setLastName("");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with wrong password - should return unauthorized")
    void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
        // Given
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPassword(passwordEncoder.encode("CorrectPassword123"));
        user.setFirstName("Existing");
        user.setLastName("User");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("existing@example.com");
        requestDto.setPassword("WrongPassword123");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Register user with password mismatch - should return bad request")
    void register_WithPasswordMismatch_ShouldReturnBadRequest() throws Exception {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Password123");
        requestDto.setRepeatPassword("DifferentPassword");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
