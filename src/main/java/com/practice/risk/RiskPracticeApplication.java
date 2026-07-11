package com.practice.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
public class RiskPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskPracticeApplication.class, args);
    }
}
