package org.example.cloudservice.infrastructure.minio;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MinioBucketInitializer {

    private static final Logger logger = LoggerFactory.getLogger(MinioBucketInitializer.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    public void init() {
        // Create a MinioClient instance using the configuration properties
        try (MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build()) {
            // Check if the bucket exists
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (bucketExists) {
                logger.info("Bucket '{}' already exists.", bucketName);
            } else {
                // Create the bucket if it doesn't exist
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Bucket '{}' created successfully at {}.", bucketName, Instant.now());
            }
        } catch (Exception e) {
            logger.error("Error initializing bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize bucket: " + bucketName, e);
        }
    }
}
