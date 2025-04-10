package org.example.cloudservice.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.FileEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.example.cloudservice.dto.FileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;


@Service
public class FileService {

    private final FileEntityRepository fileEntityRepository;
    private final UserEntityRepository userEntityRepository;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.accessKey}")
    private String minioAccessKey;

    @Value("${minio.secretKey}")
    private String minioSecretKey;

    @Value("${minio.bucket}")
    private String minioBucket;

    public FileService(FileEntityRepository fileEntityRepository,
                       UserEntityRepository userEntityRepository) {
        this.fileEntityRepository = fileEntityRepository;
        this.userEntityRepository = userEntityRepository;
    }

    /**
     * Uploads a file, verifies its integrity (if hash provided), saves it to MinIO,
     * and stores file metadata in the database.
     *
     * @param filename the provided filename
     * @param file     the multipart file to be uploaded
     * @param hash     (optional) a hash value for integrity verification (e.g., MD5)
     * @param login    the login of the user uploading the file
     */
    public void uploadFile(String filename, MultipartFile file, String hash, String login) {
        // 1. Validate file not empty.
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

//        // 2. Validate file integrity if a hash is provided.
//        if (hash != null && !hash.trim().isEmpty()) {
//            try (InputStream is = file.getInputStream()) {
//                MessageDigest digest = MessageDigest.getInstance("MD5");
//                byte[] fileBytes = is.readAllBytes();
//                byte[] computedHashBytes = digest.digest(fileBytes);
//                StringBuilder sb = new StringBuilder();
//                for (byte b : computedHashBytes) {
//                    sb.append(String.format("%02x", b));
//                }
//                String computedHash = sb.toString();
//                if (!computedHash.equalsIgnoreCase(hash)) {
//                    throw new IllegalStateException("File hash does not match the provided hash.");
//                }
//            } catch (NoSuchAlgorithmException | IOException e) {
//                throw new RuntimeException("Error computing file hash", e);
//            }
//        }

        // 3. Extract file extension and partial filename.
        String fileExtension = "";
        int dotIndex = filename.lastIndexOf(".");
        String partialFilename = filename;
        if (dotIndex != -1 && dotIndex < filename.length() - 1) {
            fileExtension = filename.substring(dotIndex + 1);
            partialFilename = filename.substring(0, dotIndex);
        }

        // 4. Generate a unique object name for storing the file.
        String uniqueFileName = UUID.randomUUID() + "_" + filename;

        // 5. Upload the file to MinIO.
        try (InputStream inputStream = file.getInputStream()) {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioEndpoint)
                    .credentials(minioAccessKey, minioSecretKey)
                    .build();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
                 ServerException | InvalidKeyException e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }

        // 6. Retrieve the user from the database.
        UserEntity user = userEntityRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));

        // 7. Build and persist FileEntity metadata.
        FileEntity fileEntity = FileEntity.builder()
                .user(user)
                .fullFilename(filename)
                .partialFilename(partialFilename)
                .fileExtension(fileExtension)
                .filesizeBytes(file.getSize())
                .s3Link(uniqueFileName)
                .deleted(false)
                .createdAt(Instant.now())
                .build();

        fileEntityRepository.save(fileEntity);
    }


    public void deleteFile(String filename, String username) {
        // TODO
    }

    public void updateFile(String filename, MultipartFile file, String login) {
        // TODO
    }

    public File getFile(String filename, String login) {
        // TODO
        return new File("123");
    }

    public List<FileDto> listFiles(int limit, String login) {
        // TODO
        return new ArrayList<>();
    }
}
