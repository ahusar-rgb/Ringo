package com.ringo.service.setup;

import com.ringo.config.ApplicationProperties;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties config;

    public void setup() {
        if(userRepository.findByUsername(config.getAdminLogin()).isPresent())
            return;

        User user = User.builder()
                .username(config.getAdminLogin())
                .email(config.getAdminLogin())
                .emailVerified(true)
                .withIdProvider(false)
                .createdAt(Instant.now())
                .name("Admin")
                .isActive(true)
                .role(Role.ROLE_ADMIN)
                .password(passwordEncoder.encode(config.getAdminPassword()))
                .build();

        userRepository.save(user);
    }
}
