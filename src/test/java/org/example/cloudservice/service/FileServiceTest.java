package org.example.cloudservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.example.cloudservice.dto.FilenameUpdateRequestDto;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.FileEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.example.cloudservice.storage.StorageAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileEntityRepository fileEntityRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private StorageAdapter storageAdapter;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileService fileService;

    private UserEntity testUser;
    private FileEntity testFileEntity;

    @BeforeEach
    void setUp() {
        // Set up a test user entity
        testUser = new UserEntity();
        testUser.setUsername("testUser");

        // Prepare a file entity as if it was previously saved.
        // In tests for uploadFile this entity is created later.
        testFileEntity = FileEntity.builder()
                .filename("test.txt")
                .hash("hash123")
                .filesizeBytes(100L)
                .deleted(false)
                .createdAt(Instant.now())
                .user(testUser)
                .s3Link("unique_test.txt")
                .build();
    }

    @Test
    void uploadFile_emptyFile_throwsIllegalArgumentException() {
        when(multipartFile.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadFile("test.txt", multipartFile, "hash123", "testUser")
        );

        assertEquals("File is empty.", exception.getMessage());
    }

    @Test
    void uploadFile_success_callsStorageAndSavesEntity() throws Exception {
        // Prepare file content and mocks.
        when(multipartFile.isEmpty()).thenReturn(false);
        byte[] fileContent = "sample content".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("text/plain");

        when(userEntityRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(testUser));

        // Call uploadFile.
        fileService.uploadFile("test.txt", multipartFile, "hash123", "testUser");

        // Verify that storage adapter is invoked with a unique filename.
        verify(storageAdapter, times(1))
                .uploadObject(anyString(), any(InputStream.class), eq((long) fileContent.length), eq("text/plain"));

        // Capture the file entity saved in the repository.
        ArgumentCaptor<FileEntity> fileEntityCaptor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileEntityRepository, times(1)).save(fileEntityCaptor.capture());

        FileEntity savedEntity = fileEntityCaptor.getValue();
        assertEquals("test.txt", savedEntity.getFilename());
        assertEquals(fileContent.length, savedEntity.getFilesizeBytes());
        assertEquals("hash123", savedEntity.getHash());
        assertFalse(savedEntity.getDeleted());
        assertNotNull(savedEntity.getS3Link());
    }

    @Test
    void deleteFile_fileExists_marksDeleted() throws Exception {
        // Prepare repository responses.
        when(userEntityRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(fileEntityRepository.findByUserAndFilenameAndDeletedFalse(testUser, "test.txt"))
                .thenReturn(Optional.of(testFileEntity));

        // Invoke deletion.
        fileService.deleteFile("test.txt", "testUser");

        // Verify that the file entity is marked as deleted.
        assertTrue(testFileEntity.getDeleted());
        assertNotNull(testFileEntity.getDeletedAt());
        verify(fileEntityRepository, times(1)).save(testFileEntity);
    }

    @Test
    void updateFilename_fileExists_updatesFilename() throws Exception {
        // Prepare mocks for an existing file.
        when(userEntityRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(fileEntityRepository.findByUserAndFilenameAndDeletedFalse(testUser, "test.txt"))
                .thenReturn(Optional.of(testFileEntity));

        FilenameUpdateRequestDto updateDto = new FilenameUpdateRequestDto("newName.txt");

        // Execute updateFilename.
        fileService.updateFilename("test.txt", updateDto, "testUser");

        // Verify the update.
        assertEquals("newName.txt", testFileEntity.getFilename());
        verify(fileEntityRepository, times(1)).save(testFileEntity);
    }

    @Test
    void getFile_fileExists_returnsFile() throws Exception {
        // Simulate downloading file content via the storage adapter.
        byte[] fileContent = "download content".getBytes();
        when(userEntityRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(fileEntityRepository.findByUserAndFilenameAndDeletedFalse(testUser, "test.txt"))
                .thenReturn(Optional.of(testFileEntity));
        when(storageAdapter.getObject("unique_test.txt"))
                .thenReturn(new ByteArrayInputStream(fileContent));

        // Call getFile.
        File downloadedFile = fileService.getFile("test.txt", "testUser");

        // Verify file creation and that the content matches.
        assertNotNull(downloadedFile);
        byte[] downloadedContent = Files.readAllBytes(downloadedFile.toPath());
        assertArrayEquals(fileContent, downloadedContent);
    }

    @Test
    void getFileHash_fileExists_returnsHash() throws Exception {
        when(userEntityRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(fileEntityRepository.findByUserAndFilenameAndDeletedFalse(testUser, "test.txt"))
                .thenReturn(Optional.of(testFileEntity));

        String hash = fileService.getFileHash("test.txt", "testUser");
        assertEquals("hash123", hash);
    }

    @Test
    void listFiles_returnsFileDtoList() {
        // Prepare a list of file entities.
        FileEntity file1 = FileEntity.builder()
                .filename("file1.txt")
                .filesizeBytes(123L)
                .deleted(false)
                .user(testUser)
                .createdAt(Instant.now())
                .build();
        FileEntity file2 = FileEntity.builder()
                .filename("file2.txt")
                .filesizeBytes(456L)
                .deleted(false)
                .user(testUser)
                .createdAt(Instant.now())
                .build();

        when(userEntityRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        // Simulate a paged repository result.
        when(fileEntityRepository.findAllByUserAndDeletedFalse(eq(testUser), any(PageRequest.class)))
                .thenReturn(Arrays.asList(file1, file2));

        List<FileDto> dtos = fileService.listFiles(10, "testUser");

        assertEquals(2, dtos.size());
        assertEquals("file1.txt", dtos.get(0).getFilename());
        assertEquals(123L, dtos.get(0).getSize());
        assertEquals("file2.txt", dtos.get(1).getFilename());
        assertEquals(456L, dtos.get(1).getSize());
    }
}
