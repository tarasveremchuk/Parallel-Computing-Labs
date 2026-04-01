package com.iotplatform.controller;

import com.iotplatform.dto.request.CreateDeviceRequest;
import com.iotplatform.dto.request.UpdateDeviceRequest;
import com.iotplatform.dto.response.*;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.model.Device;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import com.iotplatform.model.enums.Permission;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceAccessService;
import com.iotplatform.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "IoT device registration and management")
public class DeviceController {

    private final DeviceService deviceService;
    private final AuthService authService;
    private final DeviceAccessService accessService;

    @PostMapping
    @Operation(summary = "Register a new IoT device (ADMIN only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody CreateDeviceRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.registerDevice(request, currentUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "Get accessible devices (paginated, filtered)")
    public ResponseEntity<PagedResponse<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registeredAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Device> devices;
        if (status != null) {
            devices = deviceService.getDevicesByStatus(status, pageable);
        } else if (type != null) {
            devices = deviceService.getDevicesByType(type, pageable);
        } else if (location != null) {
            devices = deviceService.searchByLocation(location, pageable);
        } else {
            User currentUser = authService.getCurrentUser(authentication.getName());
            devices = deviceService.getAccessibleDevices(currentUser.getId(), pageable);
        }

        return ResponseEntity.ok(PagedResponse.from(devices, DeviceResponse::fromModel));
    }

    @GetMapping("/my")
    @Operation(summary = "Get devices owned by current user")
    public ResponseEntity<PagedResponse<DeviceResponse>> getMyDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        Page<Device> devices = deviceService.getAccessibleDevices(currentUser.getId(),
                PageRequest.of(page, size, Sort.by("registeredAt").descending()));
        return ResponseEntity.ok(PagedResponse.from(devices, DeviceResponse::fromModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(id, currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this device");
        }
        DeviceResponse response = DeviceResponse.fromModel(deviceService.getDeviceById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update device (Owner or MANAGE permission)")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeviceRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(id, currentUser.getId(), Permission.MANAGE)) {
            throw new InvalidOperationException("You don't have permission to update this device");
        }
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.updateDevice(id, request));
        return ResponseEntity.ok(ApiResponse.ok(response, "Device updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device - soft delete (Owner or ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.canManageAccess(id, currentUser.getId())) {
            throw new InvalidOperationException("You don't have permission to delete this device");
        }
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Device deleted"));
    }

    @PostMapping("/{id}/heartbeat")
    @Operation(summary = "Send device heartbeat")
    public ResponseEntity<ApiResponse<DeviceResponse>> heartbeat(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(id, currentUser.getId(), Permission.OPERATE)) {
            throw new InvalidOperationException("You don't have permission to send heartbeat for this device");
        }
        DeviceResponse response = DeviceResponse.fromModel(deviceService.heartbeat(id));
        return ResponseEntity.ok(ApiResponse.ok(response, "Heartbeat received"));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update device status (ADMIN only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam DeviceStatus status) {
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.updateStatus(id, status));
        return ResponseEntity.ok(ApiResponse.ok(response, "Status updated"));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get device statistics (avg/min/max per metric)")
    public ResponseEntity<ApiResponse<DeviceStatsResponse>> getDeviceStats(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(id, currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this device");
        }
        DeviceStatsResponse stats = deviceService.getDeviceStats(id);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}