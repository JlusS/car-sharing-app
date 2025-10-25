package com.carrental.mapper;

import com.carrental.config.MapperConfig;
import com.carrental.dto.payment.PaymentDto;
import com.carrental.dto.payment.PaymentRequestDto;
import com.carrental.model.Payment;
import com.carrental.model.Status;
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

        payment.setStatus(Status.PENDING);
        payment.setDeleted(false);

        return payment;
    }
}
