package org.example.cloudservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.example.cloudservice.dto.LoginRequestDto;
import org.example.cloudservice.dto.LoginResponseDto;
import org.example.cloudservice.service.CustomUserDetailsService;
import org.example.cloudservice.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_successful() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("user1", "password");
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAuthToken()).isEqualTo("token123");
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_invokesInvalidateToken_andReturnsOk() {
        // Arrange
        String authToken = "auth-token-value";

        // Act
        ResponseEntity<Void> response = authController.logout(authToken);

        // Assert
        verify(tokenService).invalidateToken(authToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
