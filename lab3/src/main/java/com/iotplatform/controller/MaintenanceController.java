package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.PagedResponse;
import com.iotplatform.model.MaintenanceWindow;
import com.iotplatform.model.User;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/v1/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Schedule device maintenance windows")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Schedule maintenance window (ADMIN only)")
    public ResponseEntity<ApiResponse<MaintenanceWindow>> schedule(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        MaintenanceWindow window = maintenanceService.schedule(
                UUID.fromString(body.get("deviceId")),
                LocalDateTime.parse(body.get("startTime")),
                LocalDateTime.parse(body.get("endTime")),
                body.getOrDefault("reason", null),
                user.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(window));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel maintenance window")
    public ResponseEntity<ApiResponse<MaintenanceWindow>> cancel(@PathVariable UUID id) {
        MaintenanceWindow window = maintenanceService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok(window, "Maintenance cancelled"));
    }

    @GetMapping
    @Operation(summary = "Get all maintenance windows (paginated)")
    public ResponseEntity<PagedResponse<MaintenanceWindow>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MaintenanceWindow> windows = maintenanceService.getAll(
                PageRequest.of(page, size, Sort.by("startTime").descending()));
        return ResponseEntity.ok(PagedResponse.from(windows, Function.identity()));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get maintenance windows for a device")
    public ResponseEntity<PagedResponse<MaintenanceWindow>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MaintenanceWindow> windows = maintenanceService.getByDeviceId(deviceId,
                PageRequest.of(page, size, Sort.by("startTime").descending()));
        return ResponseEntity.ok(PagedResponse.from(windows, Function.identity()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get currently active maintenance windows")
    public ResponseEntity<ApiResponse<List<MaintenanceWindow>>> getActive() {
        List<MaintenanceWindow> active = maintenanceService.getAllActive();
        return ResponseEntity.ok(ApiResponse.ok(active,
                active.size() + " device(s) under maintenance"));
    }

    @GetMapping("/device/{deviceId}/upcoming")
    @Operation(summary = "Get upcoming maintenance for a device")
    public ResponseEntity<ApiResponse<List<MaintenanceWindow>>> getUpcoming(
            @PathVariable UUID deviceId) {
        List<MaintenanceWindow> upcoming = maintenanceService.getUpcoming(deviceId);
        return ResponseEntity.ok(ApiResponse.ok(upcoming));
    }
}