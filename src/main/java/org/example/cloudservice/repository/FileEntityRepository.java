package org.example.cloudservice.repository;

import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileEntityRepository extends JpaRepository<FileEntity, Integer> {

    List<FileEntity> findByUser(UserEntity user);

    List<FileEntity> findByUserId(Integer userId);

    List<FileEntity> findByFullFilenameContaining(String searchKeyword);

    List<FileEntity> findByPartialFilenameContaining(String searchKeyword);

    List<FileEntity> findByUserAndDeletedFalse(UserEntity user);
}
