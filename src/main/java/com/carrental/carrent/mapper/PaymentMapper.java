package com.carrental.carrent.mapper;

import com.carrental.carrent.config.MapperConfig;
import com.carrental.carrent.dto.payment.PaymentDto;
import com.carrental.carrent.dto.payment.PaymentRequestDto;
import com.carrental.carrent.model.Payment;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "sessionUrl", source = "sessionUrl")
    PaymentDto toDto(Payment payment);

    default Payment toEntityWithSession(PaymentRequestDto request,
                                        Session session,
                                        BigDecimal amountToPay) {
        if (request == null && session == null) {
            return null;
        }

        Payment payment = new Payment();

        if (request != null) {
            payment.setPaymentType(request.getPaymentType());
            payment.setRentalId(request.getRentalId());
            payment.setAmountToPay(amountToPay);
        }

        if (session != null) {
            payment.setSessionId(session.getId());
            String sessionUrl = session.getUrl();
            if (sessionUrl != null && sessionUrl.length() > 255) {
                sessionUrl = sessionUrl.substring(0, 255);
            }
            payment.setSessionUrl(sessionUrl);
        }

        payment.setStatus(com.carrental.carrent.model.Status.PENDING);
        payment.setDeleted(false);

        return payment;
    }
}
