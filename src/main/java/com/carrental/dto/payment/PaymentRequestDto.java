package com.carrental.dto.payment;

import com.carrental.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @NotNull
    private Long rentalId;
    @NotNull
    private PaymentType paymentType;
}

