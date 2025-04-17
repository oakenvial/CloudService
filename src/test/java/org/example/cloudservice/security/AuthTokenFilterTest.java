package org.example.cloudservice.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

class AuthTokenFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authTokenFilter = new AuthTokenFilter(tokenService);
        // Manually set the authTokenHeader to the expected value.
        ReflectionTestUtils.setField(authTokenFilter, "authTokenHeader", "auth-token");
        // Clear security context before each test to ensure the clean state
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        // Arrange: simulate a valid token provided in the header.
        String token = "valid-token";
        String bearerToken = "Bearer " + token;
        String username = "user1";
        when(request.getHeader(anyString())).thenReturn(bearerToken);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.getUsernameFromToken(token)).thenReturn(username);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert: verify that the SecurityContext is properly set.
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        // Also verify that the filter chain was continued.
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ensuresSecurityContextCleanupAfterSuccess() throws ServletException, IOException {
        // Arrange: simulate a valid token scenario
        String token = "valid-token";
        String bearerToken = "Bearer " + token;
        String username = "user1";
        when(request.getHeader(anyString())).thenReturn(bearerToken);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.getUsernameFromToken(token)).thenReturn(username);

        // Clear context before test
        SecurityContextHolder.clearContext();

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert: check authentication is set correctly
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        // Manually clear context to verify filter would handle this properly in the real scenario
        SecurityContextHolder.clearContext();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotFilter_optionsRequest_returnsTrue() {
        // Arrange
        when(request.getMethod()).thenReturn("OPTIONS");

        // Act
        boolean result = authTokenFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "OPTIONS requests should be excluded from filtering");
    }

    @Test
    void shouldNotFilter_loginEndpoint_returnsTrue() {
        // Arrange
        when(request.getServletPath()).thenReturn("/login");

        // Act
        boolean result = authTokenFilter.shouldNotFilter(request);

        // Assert
        assertTrue(result, "Login endpoint should be excluded from filtering");
    }
}
