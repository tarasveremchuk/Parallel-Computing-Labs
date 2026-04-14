package com.iotplatform.auth.controller;

import com.iotplatform.auth.dto.request.AdminResetPasswordRequest;
import com.iotplatform.auth.dto.request.ChangeUserRoleRequest;
import com.iotplatform.auth.dto.response.ApiResponse;
import com.iotplatform.auth.dto.response.PagedResponse;
import com.iotplatform.auth.dto.response.UserResponse;
import com.iotplatform.auth.model.User;
import com.iotplatform.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management (ADMIN only)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users (paginated)")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<User> users = userService.getAllUsers(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(PagedResponse.from(users, UserResponse::fromModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(userService.getUserById(id))));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change user role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable UUID id, @Valid @RequestBody ChangeUserRoleRequest request, Authentication auth) {
        User user = userService.changeRole(id, request, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(user), "Role updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID id, Authentication auth) {
        User user = userService.deactivateUser(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromModel(user), "User deactivated"));
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "Admin resets user password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID id, @Valid @RequestBody AdminResetPasswordRequest request) {
        userService.adminResetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password reset successfully"));
    }
}