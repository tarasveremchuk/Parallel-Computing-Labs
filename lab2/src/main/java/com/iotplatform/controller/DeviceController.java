package com.iotplatform.controller;

import com.iotplatform.dto.request.CreateDeviceRequest;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.DeviceResponse;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import com.iotplatform.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "IoT device registration and management")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Register a new IoT device (ADMIN only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody CreateDeviceRequest request) {
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.registerDevice(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "Get all devices with optional filtering")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getAllDevices(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) String location) {

        List<DeviceResponse> devices;
        if (status != null) {
            devices = deviceService.getDevicesByStatus(status).stream()
                    .map(DeviceResponse::fromModel).toList();
        } else if (type != null) {
            devices = deviceService.getDevicesByType(type).stream()
                    .map(DeviceResponse::fromModel).toList();
        } else if (location != null) {
            devices = deviceService.searchByLocation(location).stream()
                    .map(DeviceResponse::fromModel).toList();
        } else {
            devices = deviceService.getAllDevices().stream()
                    .map(DeviceResponse::fromModel).toList();
        }

        return ResponseEntity.ok(ApiResponse.ok(devices,
                "Found " + devices.size() + " device(s)"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(@PathVariable UUID id) {
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.getDeviceById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/heartbeat")
    @Operation(summary = "Send device heartbeat — updates lastSeenAt and sets ONLINE")
    public ResponseEntity<ApiResponse<DeviceResponse>> heartbeat(@PathVariable UUID id) {
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.heartbeat(id));
        return ResponseEntity.ok(ApiResponse.ok(response, "Heartbeat received"));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update device status manually (ADMIN only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam DeviceStatus status) {
        DeviceResponse response = DeviceResponse.fromModel(
                deviceService.updateStatus(id, status));
        return ResponseEntity.ok(ApiResponse.ok(response, "Status updated"));
    }
}