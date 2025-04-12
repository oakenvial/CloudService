package org.example.cloudservice.service;

import lombok.Getter;
import lombok.Setter;
import org.example.cloudservice.entity.TokenEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.TokenEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final TokenEntityRepository tokenEntityRepository;
    private final UserEntityRepository userEntityRepository;

    @Value("${app.auth.token.validity.seconds}")
    @Setter
    @Getter
    private long tokenValiditySeconds;

    public TokenService(TokenEntityRepository tokenEntityRepository,
                        UserEntityRepository userEntityRepository) {
        this.tokenEntityRepository = tokenEntityRepository;
        this.userEntityRepository = userEntityRepository;
    }

    public String generateToken(String username) {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        final String token = UUID.randomUUID().toString();
        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(tokenValiditySeconds, ChronoUnit.SECONDS);
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(userEntity)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        tokenEntityRepository.save(tokenEntity);
        logger.info("Generated token for user {}: expires at {}", username, expiresAt);
        return token;
    }

    public boolean validateToken(String token) {
        Optional<TokenEntity> tokenEntityOpt = tokenEntityRepository.findByToken(token);
        if (tokenEntityOpt.isEmpty()) {
            logger.warn("Token not found: {}", token);
            return false;
        }

        TokenEntity tokenEntity = tokenEntityOpt.get();
        if (Instant.now().isAfter(tokenEntity.getExpiresAt())) {
            logger.warn("Token expired for user {}: {}", tokenEntity.getUser().getUsername(), token);
            return false;
        }
        if (tokenEntity.isRevoked()) {
            logger.warn("Token is revoked for user {}: {}", tokenEntity.getUser().getUsername(), token);
            return false;
        }
        return true;
    }

    public void invalidateToken(String token) {
        Optional<TokenEntity> tokenEntityOpt = tokenEntityRepository.findByToken(token);
        if (tokenEntityOpt.isEmpty()) {
            logger.warn("Token not found: {}", token);
        } else {
            TokenEntity tokenEntity = tokenEntityOpt.get();
            tokenEntity.setRevoked(true);
            tokenEntityRepository.save(tokenEntity);
            logger.info("Token invalidated: {}", token);
        }
    }

    public String getUsernameFromToken(String token) {
        TokenEntity tokenEntity = tokenEntityRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found in the storage: " + token));
        return tokenEntity.getUser().getUsername();
    }
}
