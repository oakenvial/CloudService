package org.example.cloudservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.cloudservice.dto.LoginRequestDto;
import org.example.cloudservice.dto.LoginResponseDto;
import org.example.cloudservice.service.CustomUserDetailsService;
import org.example.cloudservice.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(TokenService tokenService,
                          CustomUserDetailsService customUserDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user based on the supplied credentials and returns a token upon success.
     *
     * @param request the login request containing username and password; must not be null and is validated.
     * @return a ResponseEntity containing the token as a LoginResponseDto if authentication is successful, or an unauthorized status otherwise.
     */
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid @NotNull LoginRequestDto request) {
        try {
            // Load the user details using the custom service
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getLogin());

            // Verify the password using the PasswordEncoder
            if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                String token = tokenService.generateToken(request.getLogin());
                logger.info("User '{}' logged in successfully", request.getLogin());
                return ResponseEntity.ok(new LoginResponseDto(token));
            } else {
                logger.warn("Failed login attempt for user '{}'", request.getLogin());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (UsernameNotFoundException ex) {
            logger.warn("Failed login attempt for user '{}', username not found", request.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Logs out the user by invalidating the authentication token.
     *
     * @param authToken the authentication token provided in the request header; must not be null.
     * @return a ResponseEntity with HTTP 200 OK status if logout is successful.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @NotNull @RequestHeader("${app.auth.token.header:auth-token}") String authToken) {
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7); // Remove "Bearer " prefix
        }
        tokenService.invalidateToken(authToken);
        return ResponseEntity.ok().build();
    }
}
