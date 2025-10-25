package com.carrental.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carrental.config.TestConfig;
import com.carrental.dto.car.CarDto;
import com.carrental.dto.rental.RentalDto;
import com.carrental.dto.rental.RentalResponseDto;
import com.carrental.dto.rental.RentalReturnRequestDto;
import com.carrental.dto.user.UserResponseDto;
import com.carrental.service.CarService;
import com.carrental.service.RentalService;
import com.carrental.service.UserService;
import com.carrental.service.telegram.TelegramNotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
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
        "classpath:database/car/add-cars-to-table.sql",
        "classpath:database/user/add-users-to-table.sql",
        "classpath:database/rental/add-rentals-to-table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/delete-data-from-tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RentalControllerTest {
    protected static MockMvc mockMvc;

    @MockitoBean
    private RentalService rentalService;

    @MockitoBean
    private TelegramNotificationService telegramNotificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Create rental - should create rental successfully")
    void createRental_WithManagerRole_ShouldCreateRental() throws Exception {
        // Given
        RentalDto requestDto = new RentalDto();
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(7));
        requestDto.setCarId(1L);
        requestDto.setUserId(1L);

        RentalDto responseDto = new RentalDto();
        responseDto.setId(1L);
        responseDto.setRentalDate(requestDto.getRentalDate());
        responseDto.setReturnDate(requestDto.getReturnDate());
        responseDto.setCarId(requestDto.getCarId());
        responseDto.setUserId(requestDto.getUserId());

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("test@test.com");

        CarDto carResponseDto = new CarDto();
        carResponseDto.setId(1L);
        carResponseDto.setModel("Test Model");
        carResponseDto.setBrand("Test Brand");

        when(rentalService.createRental(any(RentalDto.class))).thenReturn(responseDto);
        when(userService.findById(anyLong())).thenReturn(userResponseDto);
        when(carService.findById(anyLong())).thenReturn(carResponseDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertThat(responseContent).isNotEmpty();

        RentalDto actual = objectMapper.readValue(responseContent, RentalDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getCarId(), actual.getCarId());
        assertEquals(requestDto.getUserId(), actual.getUserId());

        verify(rentalService, times(1)).createRental(any(RentalDto.class));
        verify(userService, times(1)).findById(anyLong());
        verify(carService, times(1)).findById(anyLong());
        verify(telegramNotificationService, times(1))
                .sendNewRentalNotification(any(), any(), any());
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Get rentals by user and status - should return rentals")
    void getRentalsByUserAndStatus_WithManagerRole_ShouldReturnRentals() throws Exception {
        // Given
        List<RentalResponseDto> rentals = List.of(createTestRentalResponseDto());
        when(rentalService.getRentalsByUserAndStatus(anyLong(), any(Boolean.class)))
                .thenReturn(rentals);

        // When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("user_id", "1")
                        .param("is_active", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertThat(responseContent).isNotEmpty();

        List<RentalResponseDto> actualRentals = objectMapper.readValue(
                responseContent,
                new TypeReference<List<RentalResponseDto>>() {}
        );
        assertNotNull(actualRentals);
        assertThat(actualRentals).isNotEmpty();

        verify(rentalService, times(1)).getRentalsByUserAndStatus(anyLong(), any(Boolean.class));
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get current rental - should return rental for customer")
    void getCurrentRental_WithCustomerRole_ShouldReturnRental() throws Exception {
        // Given
        RentalResponseDto rentalResponse = createTestRentalResponseDto();
        when(rentalService.getSpecificRental()).thenReturn(rentalResponse);

        // When
        MvcResult result = mockMvc.perform(get("/rentals"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertThat(responseContent).isNotEmpty();

        RentalResponseDto actual = objectMapper.readValue(responseContent, RentalResponseDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());

        verify(rentalService, times(1)).getSpecificRental();
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Get current rental - should return rental for manager")
    void getCurrentRental_WithManagerRole_ShouldReturnRental() throws Exception {
        // Given
        RentalResponseDto rentalResponse = createTestRentalResponseDto();
        when(rentalService.getSpecificRental()).thenReturn(rentalResponse);

        // When
        MvcResult result = mockMvc.perform(get("/rentals"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertThat(responseContent).isNotEmpty();

        RentalResponseDto actual = objectMapper.readValue(responseContent, RentalResponseDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());

        verify(rentalService, times(1)).getSpecificRental();
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Return rental - should update actual return date")
    void returnRental_WithCustomerRole_ShouldUpdateReturnDate() throws Exception {
        // Given
        RentalReturnRequestDto requestDto = new RentalReturnRequestDto();
        requestDto.setActualReturnDate(LocalDate.now());

        RentalResponseDto responseDto = createTestRentalResponseDto();
        responseDto.setActualReturnDate(LocalDate.parse(requestDto
                .getActualReturnDate().toString()));

        when(rentalService.returnRentalDate(any(RentalReturnRequestDto.class)))
                .thenReturn(responseDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/rentals/return")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertThat(responseContent).isNotEmpty();

        RentalResponseDto actual = objectMapper.readValue(responseContent, RentalResponseDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getActualReturnDate());

        verify(rentalService, times(1)).returnRentalDate(any(RentalReturnRequestDto.class));
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Create rental with CUSTOMER role - should return forbidden")
    void createRental_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        RentalDto requestDto = new RentalDto();
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(7));
        requestDto.setCarId(1L);
        requestDto.setUserId(1L);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Access rentals without authentication - should return unauthorized")
    void accessWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/rentals"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Create rental with invalid data - should return bad request")
    void createRental_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        RentalDto requestDto = new RentalDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "customer@test.com", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get rentals by user with CUSTOMER role - should return forbidden")
    void getRentalsByUserAndStatus_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/rentals")
                        .param("user_id", "1")
                        .param("is_active", "true"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    private RentalResponseDto createTestRentalResponseDto() {
        RentalResponseDto dto = new RentalResponseDto();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setCarId(1L);
        dto.setRentalDate(LocalDate.parse(LocalDate.now().toString()));
        dto.setReturnDate(LocalDate.parse(LocalDate.now().plusDays(1).toString()));
        dto.setActualReturnDate(null);
        return dto;
    }
}
