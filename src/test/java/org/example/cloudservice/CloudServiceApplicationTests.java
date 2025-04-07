package org.example.cloudservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CloudServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
