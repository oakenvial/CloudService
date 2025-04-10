package org.example.cloudservice.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.cloudservice.dto.FilenameUpdateRequestDto;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.FileEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.example.cloudservice.dto.FileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

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
     * Uploads a file, saves it to MinIO,
     * and stores file metadata in the database.
     *
     * @param filename the provided filename
     * @param file     the multipart file to be uploaded
     * @param hash     a hash value
     * @param username the username of the user uploading the file
     */
    public void uploadFile(String filename, MultipartFile file, String hash, String username) {
        // Validate that the file is not empty.
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        // Generate a unique object name for storing the file.
        String uniqueFileName = UUID.randomUUID() + "_" + filename;

        // Upload the file to MinIO.
        try (
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(minioEndpoint)
                        .credentials(minioAccessKey, minioSecretKey)
                        .build();
                InputStream inputStream = file.getInputStream()
        ) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }

        // Retrieve the user from the database.
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Build and persist FileEntity metadata.
        FileEntity fileEntity = FileEntity.builder()
                .user(userEntity)
                .filename(filename)
                .filesizeBytes(file.getSize())
                .hash(hash)
                .s3Link(uniqueFileName)
                .deleted(false)
                .createdAt(Instant.now())
                .build();

        fileEntityRepository.save(fileEntity);

        logger.info("File {} uploaded successfully for user {}.", filename, username);
    }

    /**
     * Marks the specified file as deleted for the given user.
     *
     * @param filename the name of the file to delete.
     * @param username the user associated with the file.
     * @throws FileNotFoundException if the file does not exist.
     */
    public void deleteFile(String filename, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntity(filename, username);

        // Safe delete the file.
        fileEntity.setDeleted(true);
        fileEntity.setDeletedAt(Instant.now());
        fileEntityRepository.save(fileEntity);

        logger.info("File {} deleted successfully for user {}.", filename, username);
    }

    /**
     * Updates the filename of a file for a given user.
     *
     * @param filename                 the current filename.
     * @param filenameUpdateRequestDto DTO containing the new filename.
     * @param username                 the user associated with the file.
     * @throws FileNotFoundException if the file does not exist.
     */
    public void updateFilename(String filename, FilenameUpdateRequestDto filenameUpdateRequestDto, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntity(filename, username);

        // Update filename
        String newFilename = filenameUpdateRequestDto.getName();
        fileEntity.setFilename(newFilename);
        fileEntityRepository.save(fileEntity);

        logger.info("Filename {} updated to {} successfully for user {}.", filename, newFilename, username);
    }

    /**
     * Downloads the file for the given user from MinIO and returns it as a temporary File.
     *
     * @param filename the name of the file.
     * @param username the user associated with the file.
     * @return a File object containing the downloaded file.
     * @throws FileNotFoundException if the file entity is not found.
     */
    public File getFile(String filename, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntity(filename, username);
        File tempFile;
        try (MinioClient minioClient = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build()) {

            // Retrieve the file using the unique s3Link stored in the entity.
            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(fileEntity.getS3Link())
                            .build()
            )) {
                // Create a temporary file and copy the retrieved data.
                tempFile = File.createTempFile("download_", "_" + fileEntity.getFilename());
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from MinIO", e);
        }
        logger.info("File {} downloaded successfully for user {}.", filename, username);
        return tempFile;
    }

    /**
     * Retrieves the hash of the specified file for a given user.
     *
     * @param filename the name of the file.
     * @param username the user associated with the file.
     * @return the hash of the file.
     * @throws FileNotFoundException if the file does not exist.
     */
    public String getFileHash(String filename, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntity(filename, username);
        return fileEntity.getHash();
    }

    /**
     * Retrieves a list of FileDto for the given user up to the specified limit.
     *
     * @param limit    the maximum number of files to return.
     * @param username the user whose files are listed.
     * @return a list of FileDto objects.
     */
    public List<FileDto> listFiles(int limit, String username) {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Pageable pageable = PageRequest.of(0, limit);

        // Retrieve all active (non-deleted) files for the user using the pageable.
        List<FileEntity> fileEntities = fileEntityRepository.findAllByUserAndDeletedFalse(userEntity, pageable);

        // Convert to DTOs while applying the limit.
        List<FileDto> fileDtos = fileEntities.stream()
                .limit(limit)
                .map(fileEntity -> new FileDto(
                        fileEntity.getFilename(),
                        fileEntity.getFilesizeBytes()))
                .collect(Collectors.toList());

        logger.info("Listed {} files for user {}.", fileDtos.size(), username);
        return fileDtos;
    }

    /**
     * A helper method that retrieves a FileEntity for the given username and filename.
     *
     * @param filename the full filename to search for.
     * @param username the username of the owner.
     * @return the corresponding FileEntity.
     * @throws FileNotFoundException if either the user or the file entity is not found.
     */
    private FileEntity getFileEntity(String filename, String username) throws FileNotFoundException {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        FileEntity fileEntity = fileEntityRepository.findByUserAndFilenameAndDeletedFalse(userEntity, filename)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + filename));
        logger.info("File {} located in the repository", filename);
        return fileEntity;
    }
}
