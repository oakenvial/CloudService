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
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        // Arrange: simulate a valid token provided in the header.
        String token = "valid-token";
        String username = "user1";
        when(request.getHeader(anyString())).thenReturn(token);
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
    void doFilterInternal_noToken_returnsUnauthorized() throws ServletException, IOException {
        // Arrange: simulate missing token header.
        when(request.getHeader(anyString())).thenReturn(null);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert: verify that the security context is cleared and 401 is set.
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Ensure the filter chain is not continued.
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_returnsUnauthorized() throws ServletException, IOException {
        // Arrange: simulate an invalid token provided in the header.
        String token = "invalid-token";
        when(request.getHeader(anyString())).thenReturn(token);
        when(tokenService.validateToken(token)).thenReturn(false);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert: verify that the security context is cleared and 401 is set.
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Ensure the filter chain is not continued.
        verify(filterChain, never()).doFilter(request, response);
    }
}
