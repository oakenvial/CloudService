package org.example.cloudservice.service;

import org.example.cloudservice.dto.FileDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    public void uploadFile(String filename, MultipartFile file, String login) {
        // TODO
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
