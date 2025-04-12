package org.example.cloudservice.storage;

import java.io.InputStream;
import org.springframework.lang.Nullable;

/**
 * Provides a contract for uploading and retrieving objects from storage.
 * Implementations should use the configured default bucket when a null bucket is provided.
 */
public interface StorageAdapter {

    /**
     * Uploads an object using the default bucket.
     *
     * @param objectName  the unique name for the object.
     * @param stream      the InputStream for the object.
     * @param size        the size of the object.
     * @param contentType the MIME type of the object.
     */
    default void uploadObject(String objectName, InputStream stream, long size, String contentType) {
        // Delegate to the bucket-specific version with a null bucket
        uploadObject(null, objectName, stream, size, contentType);
    }

    /**
     * Uploads an object using the provided bucket.
     * If {@code bucket} is null, the default bucket is used.
     *
     * @param bucket      the bucket to use, or null to use the default bucket.
     * @param objectName  the unique name for the object.
     * @param stream      the InputStream for the object.
     * @param size        the size of the object.
     * @param contentType the MIME type of the object.
     */
    void uploadObject(@Nullable String bucket, String objectName, InputStream stream, long size, String contentType);

    /**
     * Retrieves an object from the default bucket.
     *
     * @param objectName the unique name for the object.
     * @return an InputStream of the object.
     */
    default InputStream getObject(String objectName) {
        // Delegate to the bucket-specific version with a null bucket
        return getObject(null, objectName);
    }

    /**
     * Retrieves an object from the provided bucket.
     * If {@code bucket} is null, the default bucket is used.
     *
     * @param bucket     the bucket to use, or null to use the default bucket.
     * @param objectName the unique name for the object.
     * @return an InputStream of the object.
     */
    InputStream getObject(@Nullable String bucket, String objectName);
}
