package com.practice.risk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.kafka.test.context.EmbeddedKafka(partitions = 1)
class RiskPracticeApplicationTests {

    @Test
    void contextLoads() {
    }

}
