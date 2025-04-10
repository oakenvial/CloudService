package org.example.cloudservice.controller;

import org.example.cloudservice.dto.ErrorResponse;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.service.FileService;
import org.example.cloudservice.util.RandomIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.util.List;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /file
     * Uploads a file via multipart/form-data.
     */
    @PostMapping(
            value = "/file",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ErrorResponse> uploadFile(
            @RequestParam("filename") String filename,
            @RequestPart("file") MultipartFile file,
            @NonNull Principal principal) {
        try {
            fileService.uploadFile(filename, file, principal.getName());
            logger.info("File '{}' uploaded successfully for user '{}'.", filename, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("ErrorResponse uploading file for user {}: {}", principal.getName(), e.getMessage(), e);
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), RandomIdGenerator.generateRandomId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * DELETE /file?filename={filename}
     * Deletes the specified file.
     */
    @DeleteMapping(
            value = "/file",
            produces = "application/json"
    )
    public ResponseEntity<Void> deleteFile(
            @RequestParam("filename") String filename,
            @NonNull Principal principal) {
        try {
            fileService.deleteFile(filename, principal.getName());
            logger.info("File '{}' deleted successfully for user '{}'.", filename, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting file for user {}: {}", principal.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /file?filename={filename}
     * Edit file.
     */
    @PutMapping(
            value = "/file",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ErrorResponse> updateFile(
            @RequestParam("filename") String filename,
            @RequestPart("file") MultipartFile file,
            @NonNull Principal principal) {
        try {
            fileService.updateFile(filename, file, principal.getName());
            logger.info("File '{}' updated successfully for user '{}'.", filename, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating file for user {}: {}", principal.getName(), e.getMessage(), e);
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), RandomIdGenerator.generateRandomId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /file?filename={filename}
     * Retrieves the specified file.
     */
    @GetMapping(
            value = "/file",
            produces = {"multipart/form-data", "application/json"}
    )
    public ResponseEntity<File> getFile(
            @RequestParam("filename") String filename,
            @NonNull Principal principal) {
        try {
            File file = fileService.getFile(filename, principal.getName());
            if (file != null && file.exists()) {
                logger.info("File '{}' retrieved successfully for user '{}'.", filename, principal.getName());
                return ResponseEntity.ok(file);
            } else {
                logger.warn("File '{}' not found for user '{}'.", filename, principal.getName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving file '{}' for user {}: {}", filename, principal.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /list?limit={limit}
     * Retrieves a list of files, limited by the specified parameter.
     */
    @GetMapping(
            value = "/list",
            produces = "application/json"
    )
    public ResponseEntity<List<FileDto>> listFiles(
            @RequestParam("limit") Integer limit,
            @NonNull Principal principal) {
        try {
            List<FileDto> response = fileService.listFiles(limit, principal.getName());
            logger.info("Retrieved {} files for user '{}'.",
                    response != null ? response.size() : 0,
                    principal.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving file list for user {}: {}", principal.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
