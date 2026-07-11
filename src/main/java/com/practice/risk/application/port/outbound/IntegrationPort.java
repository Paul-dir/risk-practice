package com.practice.risk.application.port.outbound;

import com.practice.risk.domain.service.TaxpayerData;

import java.util.UUID;

public interface IntegrationPort {
    TaxpayerData.ExternalData getExternalData(UUID taxpayerId);
}
