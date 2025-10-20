package com.carrental.carrent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carrental.carrent.config.TestConfig;
import com.carrental.carrent.dto.payment.PaymentDto;
import com.carrental.carrent.dto.payment.PaymentRequestDto;
import com.carrental.carrent.model.PaymentType;
import com.carrental.carrent.repository.payment.PaymentRepository;
import com.carrental.carrent.service.PaymentService;
import com.carrental.carrent.service.StripeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@Sql(scripts = {
        "classpath:database/delete-data-from-tables.sql",
        "classpath:database/payment/add-payments-to-table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/delete-data-from-tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PaymentControllerTest {
    protected static MockMvc mockMvc;
    @MockitoBean
    private StripeService stripeService;

    @MockitoSpyBean
    private PaymentService paymentService;

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

    @AfterAll
    static void afterAll(@Autowired PaymentRepository paymentRepository) {
        paymentRepository.deleteAll();
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Get payments by rental ID - should return payments list")
    void getPayments_WithRentalId_ShouldReturnPayments() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/payments")
                        .param("rental_id", "1"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<PaymentDto> payments = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<PaymentDto>>() {}
        );
        assertNotNull(payments);
        assertEquals(1, payments.size());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Create payment - should return session URL")
    void createPayment_ValidRequest_ShouldReturnSessionUrl() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(3L);
        requestDto.setPaymentType(PaymentType.PAYMENT);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        doReturn("https://checkout.stripe.com/fake-session")
                .when(paymentService)
                .createPayment(any(PaymentRequestDto.class));

        // When
        MvcResult result = mockMvc.perform(
                        post("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> response = objectMapper.readValue(
                responseBody,
                new TypeReference<Map<String, String>>() {}
        );

        assertNotNull(response);
        assertNotNull(response.get("url"));
        assertEquals("https://checkout.stripe.com/fake-session", response.get("url"));
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Handle payment success - should return success message")
    void handleSuccess_ValidSessionId_ShouldReturnSuccess() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/payments/success")
                        .param("session_id", "test_session_123"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        assertEquals("Payment successful", response);
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Handle payment cancel - should return cancel message")
    void handleCancel_ShouldReturnCancelMessage() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/payments/cancel"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        assertEquals("Payment was cancelled or paused", response);
    }

    @Test
    @DisplayName("Access payments without authentication - should return unauthorized")
    void accessWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/payments").param("rental_id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "customer", authorities = {"ROLE_MANAGER"})
    @Test
    @DisplayName("Create payment with invalid data - should return bad request")
    void createPayment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                        post("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
