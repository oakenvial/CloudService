package org.example.cloudservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.example.cloudservice.dto.LoginRequestDto;
import org.example.cloudservice.dto.LoginResponseDto;
import org.example.cloudservice.service.CustomUserDetailsService;
import org.example.cloudservice.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class AuthControllerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void login_successful() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("user1", "password");
        // Build UserDetails with an encoded password for verification.
        UserDetails userDetails = User.builder()
                .username("user1")
                .password("encodedPassword")
                .authorities("ROLE_USER")
                .build();
        when(customUserDetailsService.loadUserByUsername("user1")).thenReturn(userDetails);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken("user1")).thenReturn("token123");

        // Act
        ResponseEntity<LoginResponseDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null for successful login");
        assertEquals("token123", response.getBody().getAuthToken(), "Returned token should match the generated one");
    }

    @Test
    void login_invalidPassword_returnsUnauthorized() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("user1", "wrongPassword");
        UserDetails userDetails = User.builder()
                .username("user1")
                .password("encodedPassword")
                .authorities("ROLE_USER")
                .build();
        when(customUserDetailsService.loadUserByUsername("user1")).thenReturn(userDetails);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act
        ResponseEntity<LoginResponseDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Invalid password should return 401 Unauthorized");
        assertNull(response.getBody(), "Response body should be null for unauthorized login");
    }

    @Test
    void login_userNotFound_returnsUnauthorized() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("nonexistent", "password");
        when(customUserDetailsService.loadUserByUsername("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act
        ResponseEntity<LoginResponseDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "User not found should return 401 Unauthorized");
        assertNull(response.getBody(), "Response body should be null when user is not found");
    }

    @Test
    void logout_returnsOkAndInvokesInvalidateToken() {
        // Arrange
        String authToken = "auth-token-value";

        // Act
        ResponseEntity<Void> response = authController.logout(authToken);

        // Assert
        verify(tokenService, times(1)).invalidateToken(authToken);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
