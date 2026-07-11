package com.practice.risk.application.port.outbound;

import com.practice.risk.domain.service.TaxpayerData;

import java.util.UUID;

public interface TaxReturnPort {
    /**
     * Fetch tax return data for a given taxpayer.
     * This would call the Filing Service in production.
     */
    TaxpayerData.ReturnData getReturns(UUID taxpayerId, int taxYear);
}
