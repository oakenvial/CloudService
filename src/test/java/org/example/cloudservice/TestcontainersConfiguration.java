package org.example.cloudservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static MinIOContainer minioContainer;

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withDatabaseName("cloudservice_db")
                .withUsername("test")
                .withPassword("test")
                .withExposedPorts(5432);
    }

    @Bean
    public MinIOContainer minioContainer() {
        minioContainer = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));
        minioContainer.setPortBindings(Arrays.asList("9000:9000"));
        minioContainer.start();
        return minioContainer;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Override MinIO properties with container details.
        registry.add("minio.endpoint", () -> "http://"
                + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        registry.add("minio.accessKey", () -> "minioadmin");
        registry.add("minio.secretKey", () -> "minioadmin");
        registry.add("minio.bucket", () -> "cloudservice-bucket");
    }
}
