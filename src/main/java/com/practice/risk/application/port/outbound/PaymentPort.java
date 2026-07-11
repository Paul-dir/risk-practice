package com.practice.risk.application.port.outbound;

import com.practice.risk.domain.service.TaxpayerData;

import java.util.UUID;

public interface PaymentPort {
    /**
     * Fetch payment history for a given taxpayer.
     * This would call the Payment Service in production.
     */
    TaxpayerData.PaymentData getPaymentHistory(UUID taxpayerId);
}
