package org.example.cloudservice.service;

import org.example.cloudservice.dto.FilenameUpdateRequestDto;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.FileEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.example.cloudservice.storage.StorageAdapter;
import org.example.cloudservice.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final FileEntityRepository fileEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final StorageAdapter storageAdapter;

    public FileService(FileEntityRepository fileEntityRepository,
                       UserEntityRepository userEntityRepository,
                       StorageAdapter storageAdapter) {
        this.fileEntityRepository = fileEntityRepository;
        this.userEntityRepository = userEntityRepository;
        this.storageAdapter = storageAdapter;
    }

    public void uploadFile(String filename, MultipartFile file, String hash, String username) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String uniqueFileName = UUID.randomUUID() + "_" + filename;

        // Try to perform the upload. Wrap IOExceptions in a StorageException.
        try (InputStream inputStream = file.getInputStream()) {
            storageAdapter.uploadObject(uniqueFileName, inputStream, file.getSize(), file.getContentType());
        } catch (IOException e) {
            logger.error("Error during file upload operation for user {} with filename {}", username, filename, e);
            throw new StorageException("Error uploading file to storage", e);
        }

        // Look up the user and build FileEntity metadata.
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

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

    public void deleteFile(String filename, String username) throws FileNotFoundException {
        List<FileEntity> fileEntities = getFileEntities(filename, username);
        for (FileEntity fileEntity : fileEntities) {
            fileEntity.setDeleted(true);
            fileEntity.setDeletedAt(Instant.now());
            fileEntityRepository.save(fileEntity);
        }
        logger.info("File(s) {} deleted successfully for user {}.", filename, username);
    }

    public void updateFilename(String filename, FilenameUpdateRequestDto filenameUpdateRequestDto, String username) throws FileNotFoundException {
        List<FileEntity> fileEntities = getFileEntities(filename, username);
        String newFilename = filenameUpdateRequestDto.getFilename();
        for (FileEntity fileEntity : fileEntities) {
            fileEntity.setFilename(newFilename);
            fileEntityRepository.save(fileEntity);
        }
        logger.info("Filename {} updated to {} successfully for user {}.", filename, newFilename, username);
    }

    public File getFile(String filename, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntities(filename, username).getFirst();
        File tempFile;
        // Attempt to download the file and copy it to a temporary file.
        try (InputStream inputStream = storageAdapter.getObject(fileEntity.getS3Link())) {
            tempFile = File.createTempFile("download_", "_" + fileEntity.getFilename());
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Error during file download operation for user {} with filename {}", username, filename, e);
            throw new StorageException("Error downloading file from storage", e);
        }
        logger.info("File {} downloaded successfully for user {}.", filename, username);
        return tempFile;
    }

    public String getFileHash(String filename, String username) throws FileNotFoundException {
        FileEntity fileEntity = getFileEntities(filename, username).getFirst();
        return fileEntity.getHash();
    }

    public List<FileDto> listFiles(int limit, String username) {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Pageable pageable = PageRequest.of(0, limit);
        List<FileEntity> fileEntities = fileEntityRepository.findAllByUserAndDeletedFalse(userEntity, pageable);

        List<FileDto> fileDtos = fileEntities.stream()
                .map(fe -> new FileDto(fe.getFilename(), fe.getFilesizeBytes()))
                .collect(Collectors.toList());

        logger.info("Listed {} files for user {}.", fileDtos.size(), username);
        return fileDtos;
    }

    private List<FileEntity> getFileEntities(String filename, String username) throws FileNotFoundException {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        List<FileEntity> fileEntityList = fileEntityRepository.findByUserAndFilenameAndDeletedFalse(userEntity, filename);
        if (fileEntityList.isEmpty()) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        logger.info("Total {} files of {} name located in the repository", fileEntityList.size(), filename);
        return fileEntityList;
    }
}
