package com.practice.risk.infrastructure.adapter;

import com.practice.risk.application.port.outbound.RegistrationPort;
import com.practice.risk.domain.service.TaxpayerData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class MockRegistrationAdapter implements RegistrationPort {

    @Override
    public TaxpayerData.RegistrationData getRegistration(UUID taxpayerId) {
        return TaxpayerData.RegistrationData.builder()
                .businessType("Manufacturing")
                .industryCode("C10")
                .location("Addis Ababa")
                .registrationDate(LocalDate.now().minusYears(3))
                .build();
    }
}
