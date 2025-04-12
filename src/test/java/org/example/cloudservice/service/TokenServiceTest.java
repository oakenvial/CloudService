package org.example.cloudservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.example.cloudservice.entity.TokenEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.TokenEntityRepository;
import org.example.cloudservice.repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenEntityRepository tokenEntityRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @InjectMocks
    private TokenService tokenService;

    // Set a default token validity seconds
    @BeforeEach
    void setup() {
        tokenService.setTokenValiditySeconds(3600L);
    }

    @Test
    void generateToken_UserExists_ReturnsToken() {
        // Arrange
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        String token = tokenService.generateToken(username);

        // Assert
        assertNotNull(token);
        verify(tokenEntityRepository, times(1)).save(any(TokenEntity.class));
    }

    @Test
    void generateToken_UserNotFound_ThrowsException() {
        String username = "nonExistentUser";
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> tokenService.generateToken(username));
    }

    @Test
    void validateToken_TokenNotFound_ReturnsFalse() {
        when(tokenEntityRepository.findByToken(anyString())).thenReturn(Optional.empty());

        boolean isValid = tokenService.validateToken("some-token");
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        Instant now = Instant.now();
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(user)
                .issuedAt(now.minus(2, ChronoUnit.HOURS))
                .expiresAt(now.minus(1, ChronoUnit.HOURS)) // expired one hour ago
                .revoked(false)
                .build();
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        boolean isValid = tokenService.validateToken(token);
        assertFalse(isValid);
    }

    @Test
    void validateToken_RevokedToken_ReturnsFalse() {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        Instant now = Instant.now();
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(user)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .revoked(true)
                .build();
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        boolean isValid = tokenService.validateToken(token);
        assertFalse(isValid);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        Instant now = Instant.now();
        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(user)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .revoked(false)
                .build();
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        boolean isValid = tokenService.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void invalidateToken_TokenExists_RevokesToken() {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(user)
                .revoked(false)
                .build();
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        tokenService.invalidateToken(token);

        // Verify that token's revoked flag is set to true and saved.
        assertTrue(tokenEntity.isRevoked());
        verify(tokenEntityRepository, times(1)).save(tokenEntity);
    }

    @Test
    void getUsernameFromToken_TokenExists_ReturnsUsername() {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        TokenEntity tokenEntity = TokenEntity.builder()
                .token(token)
                .user(user)
                .build();
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        String username = tokenService.getUsernameFromToken(token);
        assertEquals("testUser", username);
    }

    @Test
    void getUsernameFromToken_TokenNotFound_ThrowsException() {
        String token = "invalid-token";
        when(tokenEntityRepository.findByToken(token)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> tokenService.getUsernameFromToken(token));
        assertTrue(exception.getMessage().contains(token));
    }
}
