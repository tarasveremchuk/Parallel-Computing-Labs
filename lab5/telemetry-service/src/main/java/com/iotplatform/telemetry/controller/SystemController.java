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
import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.repository.TelemetryRepository;
import com.iotplatform.telemetry.repository.AlertRepository;
import java.util.List;

@RestController @RequestMapping("/v1/system") @RequiredArgsConstructor
public class SystemController {

    private final TelemetryService telemetryService;
    private final AlertService alertService;
    private final DeviceServiceClient deviceServiceClient;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;
    @GetMapping("/export/telemetry")
    public ResponseEntity<byte[]> exportTelemetry() {
        List<TelemetryReading> readings = telemetryRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 10000,
                        org.springframework.data.domain.Sort.by("timestamp").descending())).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("id,deviceId,metricType,value,unit,anomaly,timestamp\n");
        for (TelemetryReading r : readings) {
            csv.append(r.getId()).append(",").append(r.getDeviceId()).append(",")
                    .append(r.getMetricType()).append(",").append(r.getValue()).append(",")
                    .append(r.getUnit() != null ? r.getUnit() : "").append(",")
                    .append(r.isAnomaly()).append(",").append(r.getTimestamp()).append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=telemetry_export.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }

    @GetMapping("/export/alerts")
    public ResponseEntity<byte[]> exportAlerts() {
        List<Alert> alerts = alertRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 10000,
                        org.springframework.data.domain.Sort.by("createdAt").descending())).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("id,deviceId,metricType,severity,message,actualValue,resolved,createdAt\n");
        for (Alert a : alerts) {
            csv.append(a.getId()).append(",").append(a.getDeviceId()).append(",")
                    .append(a.getMetricType()).append(",").append(a.getSeverity()).append(",\"")
                    .append(a.getMessage() != null ? a.getMessage().replace("\"", "\"\"") : "").append("\",")
                    .append(a.getActualValue()).append(",").append(a.isResolved()).append(",")
                    .append(a.getCreatedAt()).append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=alerts_export.csv")
                .header("Content-Type", "text/csv")
                .body(csv.toString().getBytes());
    }
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