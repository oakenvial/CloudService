package org.example.cloudservice.controller;

import org.example.cloudservice.dto.FileDeleteRequest;
import org.example.cloudservice.dto.ListGet200Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;

@RestController
public class FileController {

    /**
     * DELETE /file?filename={filename}
     * Deletes the specified file.
     */
    @DeleteMapping(value = "/file", produces = "application/json")
    public ResponseEntity<Void> deleteFile(
            @RequestParam("filename") String filename,
            Principal principal) {
        // TODO: Implement file deletion logic for principal.getName()
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /file
     * Uploads a file via multipart/form-data.
     */
    @PostMapping(value = "/file", produces = "application/json", consumes = "multipart/form-data")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("filename") String filename,
            @RequestPart("file") MultipartFile file,
            Principal principal) {
        // TODO: Implement file upload logic for principal.getName()
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /file?filename={filename}
     * Updates the file details.
     */
    @PutMapping(value = "/file", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Void> updateFile(
            @RequestBody FileDeleteRequest fileDeleteRequest,
            @RequestParam("filename") String filename,
            Principal principal) {
        // TODO: Implement file update logic for principal.getName()
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /file?filename={filename}
     * Retrieves the specified file.
     */
    @GetMapping(value = "/file", produces = {"multipart/form-data", "application/json"})
    public ResponseEntity<File> getFile(
            @RequestParam("filename") String filename,
            Principal principal) {
        // TODO: Implement file retrieval logic for principal.getName()
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /list?limit={limit}
     * Retrieves a list of files, limited by the specified parameter.
     */
    @GetMapping(value = "/list", produces = "application/json")
    public ResponseEntity<ListGet200Response> listFiles(
            @RequestParam("limit") Integer limit,
            Principal principal) {
        // TODO: Implement file listing logic for principal.getName()
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
