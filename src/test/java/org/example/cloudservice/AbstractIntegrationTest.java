package org.example.cloudservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .withDatabaseName("cloudservice_db")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static MinIOContainer minio =
            new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
                    .withUserName("minioadmin")
                    .withPassword("minioadmin");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        logger.debug("Override PostgreSQL properties with container details");

        registry.add("spring.datasource.url",     postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        logger.debug("Override MinIO properties with container details");

        registry.add("minio.endpoint", () ->
                "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.accessKey", () -> "minioadmin");
        registry.add("minio.secretKey", () -> "minioadmin");
        registry.add("minio.bucket",    () -> "cloudservice-bucket");
    }
}
