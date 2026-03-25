package com.iotplatform.controller;

import com.iotplatform.dto.response.AlertResponse;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Anomaly alerts management")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get all alerts with optional filtering")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAllAlerts(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) UUID deviceId,
            @RequestParam(required = false, defaultValue = "false") boolean unresolvedOnly) {

        List<AlertResponse> alerts;
        if (unresolvedOnly) {
            alerts = alertService.getUnresolvedAlerts().stream()
                    .map(AlertResponse::fromModel).toList();
        } else if (severity != null) {
            alerts = alertService.getAlertsBySeverity(severity).stream()
                    .map(AlertResponse::fromModel).toList();
        } else if (deviceId != null) {
            alerts = alertService.getAlertsByDeviceId(deviceId).stream()
                    .map(AlertResponse::fromModel).toList();
        } else {
            alerts = alertService.getAllAlerts().stream()
                    .map(AlertResponse::fromModel).toList();
        }

        return ResponseEntity.ok(ApiResponse.ok(alerts,
                "Found " + alerts.size() + " alert(s)"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(@PathVariable UUID id) {
        AlertResponse response = AlertResponse.fromModel(
                alertService.getAlertById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve an alert (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(@PathVariable UUID id) {
        AlertResponse response = AlertResponse.fromModel(
                alertService.resolveAlert(id));
        return ResponseEntity.ok(ApiResponse.ok(response, "Alert resolved successfully"));
    }
}