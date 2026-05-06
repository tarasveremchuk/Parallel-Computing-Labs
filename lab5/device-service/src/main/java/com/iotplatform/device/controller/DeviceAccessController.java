package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.model.DeviceAccess;
import com.iotplatform.device.model.enums.Permission;
import com.iotplatform.device.service.DeviceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequiredArgsConstructor
public class DeviceAccessController {

    private final DeviceAccessService accessService;

    @GetMapping("/v1/devices/{deviceId}/access")
    public ResponseEntity<ApiResponse<List<DeviceAccess>>> getAccess(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(ApiResponse.ok(accessService.getAccessByDevice(deviceId)));
    }

    @PostMapping("/v1/devices/{deviceId}/access")
    public ResponseEntity<ApiResponse<DeviceAccess>> grant(
            @PathVariable UUID deviceId, @RequestBody Map<String, String> body) {
        DeviceAccess access = accessService.grantAccess(
                deviceId,
                UUID.fromString(body.get("userId")),
                Permission.valueOf(body.get("permission")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(access));
    }

    @PutMapping("/v1/devices/{deviceId}/access/{userId}")
    public ResponseEntity<ApiResponse<DeviceAccess>> update(
            @PathVariable UUID deviceId, @PathVariable UUID userId,
            @RequestBody Map<String, String> body) {
        DeviceAccess access = accessService.updateAccess(
                deviceId, userId, Permission.valueOf(body.get("permission")));
        return ResponseEntity.ok(ApiResponse.ok(access, "Access updated"));
    }

    @DeleteMapping("/v1/devices/{deviceId}/access/{userId}")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable UUID deviceId, @PathVariable UUID userId) {
        accessService.revokeAccess(deviceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Access revoked"));
    }
}