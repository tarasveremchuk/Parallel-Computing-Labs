package com.iotplatform.controller;

import com.iotplatform.dto.request.GrantAccessRequest;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.DeviceAccessResponse;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.model.DeviceAccess;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.Permission;
import com.iotplatform.repository.UserRepository;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/devices/{deviceId}/access")
@RequiredArgsConstructor
@Tag(name = "Device Access", description = "Manage device sharing and permissions")
public class DeviceAccessController {

    private final DeviceAccessService accessService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Grant access to a user (Owner or MANAGE permission)")
    public ResponseEntity<ApiResponse<DeviceAccessResponse>> grantAccess(
            @PathVariable UUID deviceId,
            @Valid @RequestBody GrantAccessRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.canManageAccess(deviceId, currentUser.getId())) {
            throw new InvalidOperationException("You don't have permission to manage access for this device");
        }

        DeviceAccess access = accessService.grantAccess(deviceId, request, currentUser.getId());
        String username = userRepository.findById(request.getUserId())
                .map(User::getUsername).orElse("unknown");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(DeviceAccessResponse.fromModel(access, username)));
    }

    @GetMapping
    @Operation(summary = "Get list of users with access to this device")
    public ResponseEntity<ApiResponse<List<DeviceAccessResponse>>> getAccessList(
            @PathVariable UUID deviceId,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.canManageAccess(deviceId, currentUser.getId())) {
            throw new InvalidOperationException("You don't have permission to view access for this device");
        }

        List<DeviceAccess> accessList = accessService.getDeviceAccessList(deviceId);
        List<DeviceAccessResponse> responses = accessList.stream()
                .map(a -> {
                    String username = userRepository.findById(a.getUserId())
                            .map(User::getUsername).orElse("unknown");
                    return DeviceAccessResponse.fromModel(a, username);
                }).toList();

        return ResponseEntity.ok(ApiResponse.ok(responses,
                "Found " + responses.size() + " access record(s)"));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user's permission level")
    public ResponseEntity<ApiResponse<DeviceAccessResponse>> updateAccess(
            @PathVariable UUID deviceId,
            @PathVariable UUID userId,
            @RequestParam Permission permission,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.canManageAccess(deviceId, currentUser.getId())) {
            throw new InvalidOperationException("You don't have permission to manage access for this device");
        }

        DeviceAccess access = accessService.updateAccess(deviceId, userId, permission);
        String username = userRepository.findById(userId)
                .map(User::getUsername).orElse("unknown");
        return ResponseEntity.ok(ApiResponse.ok(
                DeviceAccessResponse.fromModel(access, username), "Access updated"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Revoke user's access to device")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(
            @PathVariable UUID deviceId,
            @PathVariable UUID userId,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.canManageAccess(deviceId, currentUser.getId())) {
            throw new InvalidOperationException("You don't have permission to manage access for this device");
        }

        accessService.revokeAccess(deviceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Access revoked"));
    }
}