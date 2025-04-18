package org.example.cloudservice.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.cloudservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private AuthTokenFilter filter;

    @BeforeEach
    void setUp() {
        // configure all four @Value fields
        ReflectionTestUtils.setField(filter, "authTokenHeader", "Auth-Token");
        ReflectionTestUtils.setField(filter, "tokenPrefix", "Bearer ");
        ReflectionTestUtils.setField(filter, "loginPath", "/login");
        ReflectionTestUtils.setField(filter, "userRole", "ROLE_USER");
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_whenOnLoginPath() {
        when(request.getServletPath()).thenReturn("/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldFilter_whenNotOnLoginPath() {
        when(request.getServletPath()).thenReturn("/api/foo");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterInternal_missingHeader_sendsUnauthorized() throws IOException {
        when(request.getHeader("Auth-Token")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_malformedHeader_sendsUnauthorized() throws IOException {
        when(request.getHeader("Auth-Token")).thenReturn("bad value");

        filter.doFilterInternal(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_invalidToken_sendsUnauthorized() throws IOException {
        String token = "Bearer abc";
        when(request.getHeader("Auth-Token")).thenReturn(token);
        when(tokenService.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_exceptionDuringValidation_sendsUnauthorized() throws IOException {
        String token = "Bearer xyz";
        when(request.getHeader("Auth-Token")).thenReturn(token);
        doThrow(new RuntimeException("boom")).when(tokenService).validateToken(token);

        filter.doFilterInternal(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_validToken_setsAuthenticationAndContinues() throws IOException, ServletException {
        String token = "good token";
        String user  = "john";
        when(request.getHeader("Auth-Token")).thenReturn("Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.getUsernameFromToken(token)).thenReturn(user);

        filter.doFilterInternal(request, response, chain);

        // The filter chain must have been continued
        verify(chain).doFilter(request, response);

        // and SecurityContext must hold a UsernamePasswordAuthenticationToken
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth)
                .isInstanceOf(UsernamePasswordAuthenticationToken.class)
                .satisfies(a -> {
                    assertThat(a.getPrincipal()).isEqualTo(user);
                    assertThat(a.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                            .containsExactly("ROLE_USER");
                });
    }
}
