package com.servease.demo.support;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
public abstract class MariaDbContainerTestBase {

    private static final MariaDBContainer<?> MARIADB_CONTAINER =
            new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4"))
                    .withDatabaseName("servease")
                    .withUsername("servease")
                    .withPassword("servease");

    static {
        MARIADB_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureMariaDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MARIADB_CONTAINER::getDriverClassName);
    }
}
