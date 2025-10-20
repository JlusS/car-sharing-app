package com.carrental.carrent.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carrental.carrent.config.TestConfig;
import com.carrental.carrent.dto.car.CarDto;
import com.carrental.carrent.mapper.CarMapper;
import com.carrental.carrent.model.Car;
import com.carrental.carrent.model.CarType;
import com.carrental.carrent.repository.car.CarRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@Sql(scripts = {
        "classpath:database/delete-data-from-tables.sql",
        "classpath:database/car/add-cars-to-table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/delete-data-from-tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CarControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarMapper carMapper;

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

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get all cars - should return paginated cars list")
    void getAllCars_WithCustomerRole_ShouldReturnCars() throws Exception {
        // Given
        CarDto expectedDto = carRepository.findById(1L)
                .map(carMapper::toDto)
                .orElseThrow();

        // When
        MvcResult result = mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseContent);
        List<CarDto> cars = objectMapper.readValue(
                root.get("content").toString(),
                new TypeReference<List<CarDto>>() {}
        );

        assertThat(cars).isNotEmpty();
        CarDto actualDto = cars.get(0);

        assertThat(actualDto.getId()).isEqualTo(expectedDto.getId());
        assertThat(actualDto.getModel()).isEqualTo("Camry");
        assertThat(actualDto.getBrand()).isEqualTo("Toyota");
        assertThat(actualDto.getCarType()).isEqualTo(CarType.SEDAN);
        assertThat(actualDto.getInventory()).isEqualTo(5);
        assertThat(actualDto.getDailyFee()).isEqualTo(BigDecimal.valueOf(50.00));
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get car by ID - should return car")
    void getCarById_WithCustomerRole_ShouldReturnCar() throws Exception {
        // Given
        CarDto expectedDto = carRepository.findById(1L)
                .map(carMapper::toDto)
                .orElseThrow();

        // When
        MvcResult result = mockMvc.perform(get("/cars/{id}", expectedDto.getId()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CarDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class
        );
        assertThat(actualDto).usingRecursiveComparison().isEqualTo(expectedDto);
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Create a new car - should create car successfully")
    void createCar_WithManagerRole_ShouldCreateCar() throws Exception {
        // Given
        final long initialCount = carRepository.count();
        CarDto requestDto = new CarDto();
        requestDto.setModel("Model S");
        requestDto.setBrand("Tesla");
        requestDto.setCarType(CarType.SEDAN);
        requestDto.setInventory(3);
        requestDto.setDailyFee(BigDecimal.valueOf(100.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class
        );
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getModel(), actual.getModel());
        assertEquals(requestDto.getBrand(), actual.getBrand());
        assertEquals(initialCount + 1, carRepository.count());
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Update car - should update car successfully")
    void updateCar_WithManagerRole_ShouldUpdateCar() throws Exception {
        // Given
        Car car = carRepository.findAll().get(0);
        CarDto requestDto = new CarDto();
        requestDto.setModel("Updated Model");
        requestDto.setBrand("Updated Brand");
        requestDto.setCarType(CarType.SUV);
        requestDto.setInventory(10);
        requestDto.setDailyFee(BigDecimal.valueOf(75.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/cars/{id}", car.getId())
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class
        );
        assertNotNull(actual);
        assertEquals("Updated Model", actual.getModel());
        assertEquals("Updated Brand", actual.getBrand());
    }

    @WithMockUser(username = "manager", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Delete car by ID - should delete car successfully")
    void deleteCarById_WithManagerRole_ShouldDeleteCar() throws Exception {
        // Given
        long initialCount = carRepository.count();
        Car car = carRepository.findAll().get(0);

        // When
        mockMvc.perform(delete("/cars/{id}", car.getId()))
                .andExpect(status().isNoContent());

        // Then
        assertEquals(initialCount - 1, carRepository.count());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Create car with CUSTOMER role - should return forbidden")
    void createCar_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        CarDto requestDto = new CarDto();
        requestDto.setModel("Test Model");
        requestDto.setBrand("Test Brand");
        requestDto.setCarType(CarType.SEDAN);
        requestDto.setInventory(1);
        requestDto.setDailyFee(BigDecimal.valueOf(50.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Update car with CUSTOMER role - should return forbidden")
    void updateCar_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        CarDto requestDto = new CarDto();
        requestDto.setModel("Test Model");
        requestDto.setBrand("Test Brand");
        requestDto.setCarType(CarType.SEDAN);
        requestDto.setInventory(1);
        requestDto.setDailyFee(BigDecimal.valueOf(50.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        put("/cars/{id}", 1L)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Delete car with CUSTOMER role - should return forbidden")
    void deleteCar_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/cars/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Access without authentication - should return unauthorized")
    void accessWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/cars"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_CUSTOMER"})
    @Test
    @DisplayName("Get non-existent car - should return not found")
    void getCarById_NonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/cars/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
