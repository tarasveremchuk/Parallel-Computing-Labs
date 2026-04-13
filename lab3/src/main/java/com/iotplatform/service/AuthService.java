package com.iotplatform.service;

import com.iotplatform.dto.request.*;
import com.iotplatform.dto.response.AuthResponse;
import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.PasswordResetToken;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.UserRole;
import com.iotplatform.repository.PasswordResetTokenRepository;
import com.iotplatform.repository.UserRepository;
import com.iotplatform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already registered");
        }

        UserRole role = UserRole.VIEWER;
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("User registered: {} [{}]", user.getUsername(), user.getRole());

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new InvalidOperationException("Invalid username or password"));

        if (!user.isActive()) {
            throw new InvalidOperationException("Account is deactivated. Contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOperationException("Invalid username or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User logged in: {}", user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional
    public User updateProfile(String username, UpdateProfileRequest request) {
        User user = getCurrentUser(username);

        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        log.info("Profile updated: {}", user.getUsername());
        return user;
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getCurrentUser(username);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidOperationException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        resetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getUsername());

        // In production: send email with token. For demo: return token directly.
        return resetToken.getToken();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new InvalidOperationException("Invalid or already used reset token"));

        if (resetToken.isExpired()) {
            throw new InvalidOperationException("Reset token has expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", resetToken.getUserId()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        log.info("Password reset completed for user: {}", user.getUsername());
    }
}