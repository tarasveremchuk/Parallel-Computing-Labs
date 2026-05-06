package com.iotplatform.auth.controller;

import com.iotplatform.auth.dto.request.*;
import com.iotplatform.auth.dto.response.ApiResponse;
import com.iotplatform.auth.dto.response.AuthResponse;
import com.iotplatform.auth.dto.response.UserResponse;
import com.iotplatform.auth.model.User;
import com.iotplatform.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, profile, password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request), "Login successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication auth) {
        User user = authService.getCurrentUser(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        User user = authService.updateProfile(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(user), "Profile updated"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset token")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(token, "Password reset token generated"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password reset successfully"));
    }
    @GetMapping("/users/search")
    @Operation(summary = "Search user by email")
    public ResponseEntity<ApiResponse<UserResponse>> searchByEmail(@RequestParam String email) {
        User user = authService.getCurrentUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(user)));
    }
}