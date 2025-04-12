package org.example.cloudservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.cloudservice.service.FileService;
import org.example.cloudservice.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CloudServiceApplicationTests {

    @Autowired
    private FileService fileService;

    @Autowired
    private TokenService tokenService;

    @Test
    void contextLoads() {
        // If the context loads without exceptions and essential beans are injected,
        // then the test passes.
        assertThat(fileService).isNotNull();
        assertThat(tokenService).isNotNull();
    }
}
