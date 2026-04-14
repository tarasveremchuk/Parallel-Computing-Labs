package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.service.AlertService;
import com.iotplatform.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j @RestController @RequestMapping("/v1/dashboard") @RequiredArgsConstructor
public class DashboardController {

    private final TelemetryService telemetryService;
    private final AlertService alertService;
    private final DeviceServiceClient deviceServiceClient;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        try {
            data.put("totalDevices", deviceServiceClient.countAllDevices());
            data.put("onlineDevices", deviceServiceClient.countOnlineDevices());
        } catch (Exception e) {
            log.warn("Device Service unavailable for dashboard: {}", e.getMessage());
            data.put("totalDevices", 0);
            data.put("onlineDevices", 0);
        }

        long totalReadings = telemetryService.count();
        long anomalies = telemetryService.countAnomalies();
        data.put("totalReadings", totalReadings);
        data.put("anomalyRate", totalReadings > 0 ? Math.round((double) anomalies / totalReadings * 100) : 0);
        data.put("unresolvedAlerts", alertService.countUnresolved());
        data.put("totalAlerts", alertService.countUnresolved());

        Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
        for (AlertSeverity s : AlertSeverity.values()) {
            alertsBySeverity.put(s.name(), alertService.countBySeverity(s));
        }
        data.put("alertsBySeverity", alertsBySeverity);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}