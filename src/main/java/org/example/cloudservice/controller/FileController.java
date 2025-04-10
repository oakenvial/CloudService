package org.example.cloudservice.controller;

import org.example.cloudservice.dto.ErrorResponseDto;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.dto.FilenameUpdateRequestDto;
import org.example.cloudservice.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;

@RestController
public class FileController {

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
    public ResponseEntity<Void> uploadFile(
            @RequestParam("filename") String filename,
            @RequestPart("file") MultipartFile file,
            @RequestPart("hash") String hash,
            @NonNull Principal principal) {
        fileService.uploadFile(filename, file, hash, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
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
            @NonNull Principal principal) throws FileNotFoundException {
        fileService.deleteFile(filename, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /file?filename={filename}
     * Edit filename
     */
    @PutMapping(
            value = "/file",
            produces = "application/json",
            consumes = "application/json"
    )
    public ResponseEntity<ErrorResponseDto> updateFilename(
            @RequestParam("filename") String filename,
            @RequestBody FilenameUpdateRequestDto filenameUpdateRequestDto,
            @NonNull Principal principal) throws FileNotFoundException {
        fileService.updateFilename(filename, filenameUpdateRequestDto, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /file?filename={filename}
     * Retrieves the specified file.
     */
    @GetMapping(
            value = "/file",
            produces = "multipart/form-data"
    )
    public ResponseEntity<MultiValueMap<String, Object>> getFile(
            @RequestParam("filename") String filename,
            @NonNull Principal principal) throws FileNotFoundException {
        File file = fileService.getFile(filename, principal.getName());
        String fileHash = fileService.getFileHash(filename, principal.getName());

        // Create a resource for the file binary.
        Resource fileResource = new FileSystemResource(file);

        // Build a multipart response that contains two parts: "hash" and "file".
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("hash", fileHash);
        multipartBody.add("file", fileResource);

        return ResponseEntity.ok().body(multipartBody);
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
        List<FileDto> response = fileService.listFiles(limit, principal.getName());
        return ResponseEntity.ok(response);
    }
}
