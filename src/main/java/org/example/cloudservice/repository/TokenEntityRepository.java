package org.example.cloudservice.repository;

import org.example.cloudservice.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenEntityRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByToken(String token);
}
