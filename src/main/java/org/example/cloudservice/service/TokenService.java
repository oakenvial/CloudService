package org.example.cloudservice.service;

import org.example.cloudservice.entity.TokenEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.TokenEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private static final long TOKEN_VALIDITY_SECONDS = 3600;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final TokenEntityRepository tokenEntityRepository;
    private final UserEntityRepository userEntityRepository;

    public TokenService(TokenEntityRepository tokenEntityRepository,
                        UserEntityRepository userEntityRepository) {
        this.tokenEntityRepository = tokenEntityRepository;
        this.userEntityRepository = userEntityRepository;
    }

    public String generateToken(String username) {
        Optional<UserEntity> userEntityOpt = userEntityRepository.findByLogin(username);
        if (userEntityOpt.isPresent()) {
            final String token = UUID.randomUUID().toString();
            final Instant issuedAt = Instant.now();
            final Instant expiresAt = issuedAt.plus(TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS);

            TokenEntity tokenEntity = TokenEntity.builder()
                    .token(token)
                    .user(userEntityOpt.get())
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .revoked(false)
                    .build();
            tokenEntityRepository.save(tokenEntity);
            logger.debug("Generated token for user {}: expires at {}", username, expiresAt);
            return token;
        } else {
            logger.error("Error while generating token: couldn't find user {}", username);
            throw new UsernameNotFoundException("User not found with login: " + username);
        }
    }

    public boolean validateToken(String token) {
        Optional<TokenEntity> tokenEntityOpt = tokenEntityRepository.findByToken(token);
        if (tokenEntityOpt.isEmpty()) {
            logger.debug("Token not found: {}", token);
            return false;
        }

        TokenEntity tokenEntity = tokenEntityOpt.get();
        if (Instant.now().isAfter(tokenEntity.getExpiresAt())) {
            logger.debug("Token expired for user {}: {}", tokenEntity.getUser().getLogin(), token);
            return false;
        }
        if (tokenEntity.isRevoked()) {
            logger.debug("Token is revoked for user {}: {}", tokenEntity.getUser().getLogin(), token);
            return false;
        }
        return true;
    }

    public void invalidateToken(String token) {
        Optional<TokenEntity> tokenEntityOpt = tokenEntityRepository.findByToken(token);
        if (tokenEntityOpt.isEmpty()) {
            logger.debug("Token not found: {}", token);
        } else {
            TokenEntity tokenEntity = tokenEntityOpt.get();
            tokenEntity.setRevoked(true);
            tokenEntityRepository.save(tokenEntity);
            logger.debug("Token invalidated: {}", token);
        }
    }

    public String getLoginFromToken(String token) {
        Optional<TokenEntity> tokenEntityOpt = tokenEntityRepository.findByToken(token);
        if (tokenEntityOpt.isPresent()) {
            return tokenEntityOpt.get().getUser().getLogin();
        } else {
            logger.error("Error while getting login from token: {}", token);
            throw new UsernameNotFoundException("User not found with token: " + token);
        }
    }
}
