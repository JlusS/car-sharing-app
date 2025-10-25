package com.carrental.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carrental.config.TestConfig;
import com.carrental.dto.user.UserResponseDto;
import com.carrental.dto.user.UserRoleUpdateDto;
import com.carrental.dto.user.UserUpdateRequestDto;
import com.carrental.model.Role;
import com.carrental.model.User;
import com.carrental.repository.user.UserRepository;
import com.carrental.security.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@Sql(scripts = {
        "classpath:database/delete-data-from-tables.sql",
        "classpath:database/user/add-users-to-table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/delete-data-from-tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerTest {
    protected static MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(applicationContext)
                    .apply(springSecurity())
                    .build();
        }
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Update user role - should update role successfully")
    void updateUserRole_WithManagerRole_ShouldUpdateRole() throws Exception {
        // Given
        User user = userRepository.findAll().get(0);
        UserRoleUpdateDto roleUpdateDto = new UserRoleUpdateDto();
        roleUpdateDto.setRole(Role.MANAGER);

        String jsonRequest = objectMapper.writeValueAsString(roleUpdateDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/{userId}/role", user.getId())
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
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get current user - should return user info")
    void getUserById_WithCustomerRole_ShouldReturnUser() throws Exception {
        // Given
        User existingUser = userRepository.findByEmail("customer@test.com")
                .orElseThrow(() -> new RuntimeException("Test user not found in DB"));

        when(authenticationService.getAuthenticatedUser()).thenReturn(existingUser);

        // When
        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        assertNotNull(actual);
        assertNotNull(actual.getEmail());
        assertEquals("customer@test.com", actual.getEmail());

        verify(authenticationService, times(1)).getAuthenticatedUser();
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Update user information - should update successfully")
    void updateUser_WithCustomerRole_ShouldUpdateUser() throws Exception {
        // Given
        User existingUser = userRepository.findByEmail("customer@test.com")
                .orElseThrow(() -> new RuntimeException("Test user not found in DB"));

        when(authenticationService.getAuthenticatedUser()).thenReturn(existingUser);

        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setEmail(existingUser.getUsername());
        requestDto.setFirstName("UpdatedFirstName");
        requestDto.setLastName("UpdatedLastName");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/me")
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
        assertEquals("UpdatedFirstName", actual.getFirstName());
        assertEquals("UpdatedLastName", actual.getLastName());

        User updatedUser = userRepository.findByEmail("customer@test.com").orElseThrow();
        assertEquals("UpdatedFirstName", updatedUser.getFirstName());
        assertEquals("UpdatedLastName", updatedUser.getLastName());

        verify(authenticationService, times(1)).getAuthenticatedUser();
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Update user role with CUSTOMER role - should return forbidden")
    void updateUserRole_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        UserRoleUpdateDto roleUpdateDto = new UserRoleUpdateDto();
        roleUpdateDto.setRole(Role.MANAGER);

        String jsonRequest = objectMapper.writeValueAsString(roleUpdateDto);

        mockMvc.perform(
                        put("/users/{userId}/role", 1L)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Access user endpoints without authentication - should return unauthorized")
    void accessWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Update user with invalid data - should return bad request")
    void updateUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("firstName", null);
        invalidRequest.put("lastName", null);

        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);

        // When & Then
        mockMvc.perform(
                        put("/users/me")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
