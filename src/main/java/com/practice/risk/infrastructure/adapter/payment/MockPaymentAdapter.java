package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.PaymentPort;
import com.practice.risk.domain.service.TaxpayerData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentAdapter implements PaymentPort {

    @Override
    public TaxpayerData.PaymentData getPaymentHistory(UUID taxpayerId) {
        return TaxpayerData.PaymentData.builder()
                .latePaymentDays(30)
                .totalOwed(BigDecimal.valueOf(1_000_000))
                .totalPaid(BigDecimal.valueOf(750_000))
                .numberOfLatePayments(4)
                .build();
    }
}
