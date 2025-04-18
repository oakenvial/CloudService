package org.example.cloudservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;

@RestController
@Validated
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /file
     * Uploads a file via multipart/form-data.
     *
     * @param filename  the name of the file; must not be null.
     * @param file      the multipart file payload; must not be null.
     * @param hash      the file hash; optional. If omitted, the service may compute or ignore it.
     * @param principal the authenticated principal; must not be null.
     * @return HTTP 200 OK if successful.
     */
    @PostMapping(
            value = "/file",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Void> uploadFile(
            @NotNull @RequestParam("filename") String filename,
            @NotNull @RequestPart("file") MultipartFile file,
            @RequestPart(value = "hash", required = false) String hash,
            @NonNull Principal principal) {

        fileService.uploadFile(filename, file, hash, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * DELETE /file?filename={filename}
     * Deletes the specified file.
     *
     * @param filename  the name of the file to delete; must not be null.
     * @param principal the authenticated principal; must not be null.
     * @return HTTP 200 OK if deletion is successful.
     * @throws FileNotFoundException if the file is not found.
     */
    @DeleteMapping(
            value = "/file",
            produces = "application/json"
    )
    public ResponseEntity<Void> deleteFile(
            @NotNull @RequestParam("filename") String filename,
            @NonNull Principal principal) throws FileNotFoundException {
        fileService.deleteFile(filename, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /file?filename={filename}
     * Edits the filename.
     *
     * @param filename                  the current filename; must not be null.
     * @param filenameUpdateRequestDto  DTO containing the new filename; validated.
     * @param principal                 the authenticated principal; must not be null.
     * @return HTTP 200 OK if the update is successful.
     * @throws FileNotFoundException if the file is not found.
     */
    @PutMapping(
            value = "/file",
            produces = "application/json",
            consumes = "application/json"
    )
    public ResponseEntity<ErrorResponseDto> updateFilename(
            @NotNull @RequestParam("filename") String filename,
            @Valid @RequestBody FilenameUpdateRequestDto filenameUpdateRequestDto,
            @NonNull Principal principal) throws FileNotFoundException {

        fileService.updateFilename(filename, filenameUpdateRequestDto, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /file?filename={filename}
     * Retrieves the specified file along with its hash in a multipart/form-data response.
     *
     * @param filename  the name of the file to retrieve; must not be null.
     * @param principal the authenticated principal; must not be null.
     * @return a multipart response with a "hash" and a "file" part.
     * @throws FileNotFoundException if the file is not found.
     */
    @GetMapping(
            value = "/file",
            produces = "multipart/form-data"
    )
    public ResponseEntity<MultiValueMap<String, Object>> getFile(
            @NotNull @RequestParam("filename") String filename,
            @NonNull Principal principal) throws FileNotFoundException {

        File file = fileService.getFile(filename, principal.getName());
        String fileHash = fileService.getFileHash(filename, principal.getName());

        Resource fileResource = new FileSystemResource(file);
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("hash", fileHash);
        multipartBody.add("file", fileResource);

        return ResponseEntity.ok(multipartBody);
    }

    /**
     * GET /list?limit={limit}
     * Retrieves a list of files, limited by the specified parameter.
     *
     * @param limit     the maximum number of files to return; must not be null.
     * @param principal the authenticated principal; must not be null.
     * @return a JSON response with a list of file DTOs.
     */
    @GetMapping(
            value = "/list",
            produces = "application/json"
    )
    public ResponseEntity<List<FileDto>> listFiles(
            @NotNull @RequestParam("limit") Integer limit,
            @NonNull Principal principal) {

        List<FileDto> response = fileService.listFiles(limit, principal.getName());
        return ResponseEntity.ok(response);
    }
}
