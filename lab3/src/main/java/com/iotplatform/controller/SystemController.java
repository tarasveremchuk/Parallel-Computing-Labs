package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.model.User;
import com.iotplatform.repository.*;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceAccessService;
import com.iotplatform.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "System health and data export")
public class SystemController {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;
    private final ExportService exportService;
    private final AuthService authService;
    private final DeviceAccessService accessService;

    @GetMapping("/health")
    @Operation(summary = "System health check (public)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        try {
            long userCount = userRepository.count();
            long deviceCount = deviceRepository.countByDeletedFalse();
            long telemetryCount = telemetryRepository.count();
            long alertCount = alertRepository.count();

            health.put("database", "CONNECTED");
            health.put("users", userCount);
            health.put("devices", deviceCount);
            health.put("telemetryReadings", telemetryCount);
            health.put("alerts", alertCount);
        } catch (Exception e) {
            health.put("database", "DISCONNECTED");
            health.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok(health));
    }

    @GetMapping("/export/telemetry")
    @Operation(summary = "Export telemetry data as CSV")
    public ResponseEntity<byte[]> exportTelemetry(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(user.getId());
        String csv = exportService.exportTelemetryCsv(deviceIds);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=telemetry_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    @GetMapping("/export/alerts")
    @Operation(summary = "Export alerts data as CSV")
    public ResponseEntity<byte[]> exportAlerts(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(user.getId());
        String csv = exportService.exportAlertsCsv(deviceIds);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alerts_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}