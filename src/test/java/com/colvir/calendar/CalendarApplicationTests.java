package com.colvir.calendar;

import com.colvir.calendar.config.RabbitConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ContextConfiguration(classes = {
        RabbitConfig.class
})
@ComponentScan
class CalendarApplicationTests {

    private static final PostgresTestContainer POSTGRES_CONTAINER = PostgresTestContainer.getInstance();

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        POSTGRES_CONTAINER.start();
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }

    @AfterAll
    static void stopContainer() {
        POSTGRES_CONTAINER.stop();
    }

    @Test
    void contextLoads() {
    }

}
