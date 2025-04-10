package org.example.cloudservice.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomIdGenerator {
    private RandomIdGenerator() {
        // Private constructor to prevent instantiation
    }

    public static int generateRandomId() {
        // Generates a random integer between 1 (inclusive) and Integer.MAX_VALUE (exclusive)
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }
}
