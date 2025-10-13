package com.carrental.carrent.dto.payment;

import com.carrental.carrent.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @NotNull
    private Long rentalId;
    @NotNull
    private PaymentType paymentType;
}

