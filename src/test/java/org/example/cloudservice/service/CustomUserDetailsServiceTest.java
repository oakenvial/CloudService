package org.example.cloudservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.UserEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Given
        String username = "testUser";
        String encodedPassword = "encodedPassword";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(encodedPassword);

        // Configure the repository to return the user entity.
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails, "UserDetails should not be null when the user exists");
        assertEquals(username, userDetails.getUsername(), "Username should match the user entity value");
        assertEquals(encodedPassword, userDetails.getPassword(), "Password should match the encoded value from user entity");

        // Verify that the repository method was invoked exactly once.
        verify(userEntityRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ThrowsUsernameNotFoundException() {
        // Given
        String username = "nonExistentUser";
        when(userEntityRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username),
                "Expected UsernameNotFoundException when user is not found"
        );
        assertTrue(exception.getMessage().contains(username), "Exception message should mention the username");

        verify(userEntityRepository, times(1)).findByUsername(username);
    }
}
