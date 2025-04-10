package org.example.cloudservice.controller;

import org.example.cloudservice.dto.LoginRequestDto;
import org.example.cloudservice.dto.LoginResponseDto;
import org.example.cloudservice.service.CustomUserDetailsService;
import org.example.cloudservice.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @NonNull LoginRequestDto request) {
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
            logger.warn("Failed login attempt for user '{}'", request.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @NonNull @RequestHeader("${app.auth.token.header:auth-token}") String authToken) {
        tokenService.invalidateToken(authToken);
        return ResponseEntity.ok().build();
    }
}
