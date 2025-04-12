package org.example.cloudservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class PostgresContainerTests {

    @Container
    @ServiceConnection
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("cloudservice_db")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432);

    @Test
    void testPostgresContainerIsRunning() {
        assertThat(postgresContainer.isRunning()).isTrue();
    }
}
