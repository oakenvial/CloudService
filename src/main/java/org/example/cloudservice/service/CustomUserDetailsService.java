package org.example.cloudservice.service;

import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.UserEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserEntityRepository userEntityRepository;

    public CustomUserDetailsService(final UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        logger.debug("User {} found, loading details", username);

        // Mapping of UserEntity to Spring Security's User.
        // The password stored in UserEntity is encoded
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .authorities("ROLE_USER")
                .build();
    }
}
