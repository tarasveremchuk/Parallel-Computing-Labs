package com.iotplatform.auth.config;

import com.iotplatform.auth.model.User;
import com.iotplatform.auth.model.enums.UserRole;
import com.iotplatform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("=== Auth DB already has data, skipping ===");
            return;
        }

        log.info("=== Initializing auth data ===");

        userRepository.save(User.builder()
                .username("admin").email("admin@iotplatform.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN).active(true).build());
        log.info("Admin created: admin / admin123");

        userRepository.save(User.builder()
                .username("operator").email("operator@iotplatform.com")
                .password(passwordEncoder.encode("oper123"))
                .role(UserRole.OPERATOR).active(true).build());
        log.info("Operator created: operator / oper123");

        userRepository.save(User.builder()
                .username("viewer").email("viewer@iotplatform.com")
                .password(passwordEncoder.encode("view123"))
                .role(UserRole.VIEWER).active(true).build());
        log.info("Viewer created: viewer / view123");

        log.info("=== Auth data initialized: 3 users ===");
    }
}