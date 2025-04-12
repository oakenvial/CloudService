package org.example.cloudservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class MinioContainerTests {

    @Container
    @ServiceConnection
    public MinIOContainer minioContainer = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

    @Test
    void testMinioContainerIsRunning() {
        assertThat(minioContainer.isRunning()).isTrue();
    }
}
