package org.example.cloudservice.repository;

import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FileEntityRepository extends JpaRepository<FileEntity, Integer> {

    List<FileEntity> findByUserAndFilenameAndDeletedFalse(UserEntity user, String filename);

    List<FileEntity> findAllByUserAndDeletedFalse(UserEntity userEntity, Pageable pageable);
}
