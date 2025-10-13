package com.carrental.carrent.dto.payment;

import com.carrental.carrent.model.PaymentType;
import com.carrental.carrent.model.Status;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentDto {
    @NotNull
    private Long id;

    @NotNull
    private BigDecimal amountToPay;

    @NotNull
    private Status status;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    private Long rentalId;

    private String sessionId;
    private String sessionUrl;
}

