package org.example.cloudservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.example.cloudservice.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    private static final String AUTH_TOKEN_HEADER = "auth-token";
    private final TokenService tokenService;

    public AuthTokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip token validation for /login endpoint
        return "/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(AUTH_TOKEN_HEADER);
        if (token != null && tokenService.validateToken(token)) {
            String login = tokenService.getLoginFromToken(token);
            logger.debug("Token valid for user: {}", login);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    login,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            if (token == null) {
                logger.debug("No auth-token header found.");
            } else {
                logger.warn("Invalid token provided.");
            }
            // Clear any existing authentication context
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // End filter chain here as authentication failed
        }

        filterChain.doFilter(request, response);
    }
}
