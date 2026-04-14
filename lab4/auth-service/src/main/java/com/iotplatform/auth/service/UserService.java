package com.iotplatform.auth.service;

import com.iotplatform.auth.dto.request.AdminResetPasswordRequest;
import com.iotplatform.auth.dto.request.ChangeUserRoleRequest;
import com.iotplatform.auth.exception.InvalidOperationException;
import com.iotplatform.auth.exception.ResourceNotFoundException;
import com.iotplatform.auth.model.User;
import com.iotplatform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public User changeRole(UUID id, ChangeUserRoleRequest request, String adminUsername) {
        User user = getUserById(id);
        if (user.getUsername().equalsIgnoreCase(adminUsername)) {
            throw new InvalidOperationException("Cannot change your own role");
        }
        user.setRole(request.getRole());
        userRepository.save(user);
        log.info("User {} role changed to {} by {}", user.getUsername(), request.getRole(), adminUsername);
        return user;
    }

    @Transactional
    public User deactivateUser(UUID id, String adminUsername) {
        User user = getUserById(id);
        if (user.getUsername().equalsIgnoreCase(adminUsername)) {
            throw new InvalidOperationException("Cannot deactivate your own account");
        }
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated by {}", user.getUsername(), adminUsername);
        return user;
    }

    @Transactional
    public void adminResetPassword(UUID id, AdminResetPasswordRequest request) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset by admin for user: {}", user.getUsername());
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }
}