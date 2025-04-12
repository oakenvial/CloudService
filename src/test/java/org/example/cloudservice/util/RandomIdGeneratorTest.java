package org.example.cloudservice.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomIdGeneratorTest {

    @Test
    void testGenerateRandomId_inRange() {
        // Generate a series of ids and ensure they are within the expected range.
        for (int i = 0; i < 1000; i++) {
            int randomId = RandomIdGenerator.generateRandomId();
            assertTrue(randomId >= 1, "Random ID should be at least 1");
            assertTrue(randomId < Integer.MAX_VALUE, "Random ID should be less than Integer.MAX_VALUE");
        }
    }
}
