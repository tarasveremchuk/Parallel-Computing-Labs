package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.service.TelemetryService;
import com.iotplatform.telemetry.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController @RequestMapping("/v1/system") @RequiredArgsConstructor
public class SystemController {

    private final TelemetryService telemetryService;
    private final AlertService alertService;
    private final DeviceServiceClient deviceServiceClient;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("telemetryReadings", telemetryService.count());
        health.put("unresolvedAlerts", alertService.countUnresolved());

        try {
            health.put("devices", deviceServiceClient.countAllDevices());
            health.put("deviceService", "UP");
        } catch (Exception e) {
            health.put("devices", "N/A");
            health.put("deviceService", "DOWN");
        }

        return ResponseEntity.ok(ApiResponse.ok(health));
    }
}