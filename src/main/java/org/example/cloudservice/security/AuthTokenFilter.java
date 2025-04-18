package org.example.cloudservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudservice.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final TokenService tokenService;

    @Value("${app.auth.token-header:Auth-Token}")
    private String authTokenHeader;

    @Value("${app.auth.token-prefix:Bearer }")
    private String tokenPrefix;

    @Value("${app.auth.login-path:/login}")
    private String loginPath;

    @Value("${app.auth.user-role:ROLE_USER}")
    private String userRole;

    public AuthTokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return loginPath.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws IOException {

        String header = req.getHeader(authTokenHeader);
        if (header == null || !header.startsWith(tokenPrefix)) {
            logger.debug("Missing or malformed '{}' header on {}", authTokenHeader, req.getRequestURI());
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
            return;
        }

        String token = header.substring(tokenPrefix.length());
        try {
            if (!tokenService.validateToken(token)) {
                logger.warn("Invalid token on {}", req.getRequestURI());
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            String username = tokenService.getUsernameFromToken(token);
            logger.debug("Authenticated user '{}'", username);

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(userRole))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(req, res);
        } catch (Exception ex) {
            logger.error("Error validating token on {}", req.getRequestURI(), ex);
            SecurityContextHolder.clearContext();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token processing error");
        }
    }
}
