package org.example.cloudservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.example.cloudservice.dto.ErrorResponseDto;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.dto.FilenameUpdateRequestDto;
import org.example.cloudservice.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    // Use a simple Principal implementation.
    private Principal principal;

    @BeforeEach
    void setUp() {
        // Using lambda; getName returns "testUser"
        principal = () -> "testUser";
    }

    @Test
    void uploadFile_returnsOk() {
        // Arrange
        // Create a mock MultipartFile.
        MultipartFile file = mock(MultipartFile.class);

        // Act
        ResponseEntity<Void> response = fileController.uploadFile("test.txt", file, "hash123", principal);

        // Assert
        verify(fileService, times(1)).uploadFile("test.txt", file, "hash123", "testUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteFile_returnsOk() throws Exception {
        // Arrange
        doNothing().when(fileService).deleteFile("test.txt", "testUser");

        // Act
        ResponseEntity<Void> response = fileController.deleteFile("test.txt", principal);

        // Assert
        verify(fileService, times(1)).deleteFile("test.txt", "testUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateFilename_returnsOk() throws Exception {
        // Arrange
        FilenameUpdateRequestDto updateDto = new FilenameUpdateRequestDto("newName.txt");
        doNothing().when(fileService).updateFilename("test.txt", updateDto, "testUser");

        // Act
        ResponseEntity<ErrorResponseDto> response = fileController.updateFilename("test.txt", updateDto, principal);

        // Assert
        verify(fileService, times(1)).updateFilename("test.txt", updateDto, "testUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody(), "Success response should have null body");
    }

    @Test
    void getFile_returnsMultipartResponse() throws Exception {
        // Arrange
        // Create a temporary file to simulate a file returned by fileService.getFile.
        File dummyFile = File.createTempFile("dummy", ".txt");
        String expectedHash = "hash123";
        when(fileService.getFile("test.txt", "testUser")).thenReturn(dummyFile);
        when(fileService.getFileHash("test.txt", "testUser")).thenReturn(expectedHash);

        // Act
        ResponseEntity<MultiValueMap<String, Object>> response = fileController.getFile("test.txt", principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MultiValueMap<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        // Verify that body contains "hash" and "file"
        assertTrue(body.containsKey("hash"));
        assertEquals(expectedHash, body.getFirst("hash"));
        assertTrue(body.containsKey("file"));
        Object fileResource = body.getFirst("file");
        assertInstanceOf(FileSystemResource.class, fileResource);
        FileSystemResource resource = (FileSystemResource) fileResource;
        assertEquals(dummyFile.getAbsolutePath(), resource.getFile().getAbsolutePath());
    }

    @Test
    void listFiles_returnsFileDtoList() {
        // Arrange
        List<FileDto> fileDtos = Arrays.asList(
                new FileDto("file1.txt", 100L),
                new FileDto("file2.txt", 200L)
        );
        when(fileService.listFiles(10, "testUser")).thenReturn(fileDtos);

        // Act
        ResponseEntity<List<FileDto>> response = fileController.listFiles(10, principal);

        // Assert
        verify(fileService, times(1)).listFiles(10, "testUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fileDtos, response.getBody());
    }

    private static Stream<Arguments> provideFileListLimits() {
        return Stream.of(
            Arguments.of(5, Arrays.asList(new FileDto("file1.txt", 100L))),
            Arguments.of(10, Arrays.asList(new FileDto("file1.txt", 100L), new FileDto("file2.txt", 200L))),
            Arguments.of(20, Arrays.asList(new FileDto("file1.txt", 100L), new FileDto("file2.txt", 200L), new FileDto("file3.txt", 300L)))
        );
    }

    @ParameterizedTest
    @MethodSource("provideFileListLimits")
    void listFiles_withDifferentLimits_returnsCorrectNumberOfFiles(int limit, List<FileDto> expectedFiles) {
        // Arrange
        when(fileService.listFiles(limit, "testUser")).thenReturn(expectedFiles);

        // Act
        ResponseEntity<List<FileDto>> response = fileController.listFiles(limit, principal);

        // Assert
        verify(fileService, times(1)).listFiles(limit, "testUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedFiles, response.getBody());
        assertEquals(expectedFiles.size(), response.getBody().size());
    }
}
