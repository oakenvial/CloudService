package org.example.cloudservice.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioStorageAdapter implements StorageAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MinioStorageAdapter.class);

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Value("${minio.bucket}")
    private String defaultBucket;

    @Override
    public void uploadObject(@Nullable String bucket, String objectName, InputStream stream, long size, String contentType) {
        String targetBucket = (bucket != null) ? bucket : defaultBucket;
        logger.debug("Uploading object '{}' to bucket '{}'", objectName, targetBucket);

        // Create a new client instance.
        MinioClient client = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();

        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(objectName)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
            logger.info("Successfully uploaded object '{}' to bucket '{}'", objectName, targetBucket);
        } catch (Exception e) {
            logger.error("Error uploading object '{}' to bucket '{}'", objectName, targetBucket, e);
            throw new StorageException("Error uploading file to MinIO", e);
        }
    }

    @Override
    public InputStream getObject(@Nullable String bucket, String objectName) {
        String targetBucket = (bucket != null) ? bucket : defaultBucket;
        logger.debug("Retrieving object '{}' from bucket '{}'", objectName, targetBucket);

        // Create a new client instance.
        MinioClient client = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();

        try {
            InputStream inputStream = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .build()
            );
            logger.info("Successfully retrieved object '{}' from bucket '{}'", objectName, targetBucket);
            return inputStream;
        } catch (Exception e) {
            logger.error("Error retrieving object '{}' from bucket '{}'", objectName, targetBucket, e);
            throw new StorageException("Error downloading file from MinIO", e);
        }
    }
}
