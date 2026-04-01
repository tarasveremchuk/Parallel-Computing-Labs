package com.iotplatform.controller;

import com.iotplatform.dto.request.ResolveAlertRequest;
import com.iotplatform.dto.response.*;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.model.enums.Permission;
import com.iotplatform.service.AlertService;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Anomaly alerts management")
public class AlertController {

    private final AlertService alertService;
    private final AuthService authService;
    private final DeviceAccessService accessService;

    @GetMapping
    @Operation(summary = "Get alerts (paginated, filtered, accessible devices only)")
    public ResponseEntity<PagedResponse<AlertResponse>> getAllAlerts(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) UUID deviceId,
            @RequestParam(required = false, defaultValue = "false") boolean unresolvedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {

        User currentUser = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(currentUser.getId());

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Alert> alerts;
        if (deviceId != null) {
            if (!accessService.hasAccess(deviceId, currentUser.getId(), Permission.READ)) {
                throw new InvalidOperationException("You don't have access to this device");
            }
            alerts = alertService.getAlertsByDeviceId(deviceId, pageable);
        } else if (unresolvedOnly) {
            alerts = alertService.getUnresolvedByAccessibleDevices(deviceIds, pageable);
        } else if (severity != null) {
            alerts = alertService.getBySeverityAndAccessibleDevices(severity, deviceIds, pageable);
        } else {
            alerts = alertService.getAlertsByAccessibleDevices(deviceIds, pageable);
        }

        return ResponseEntity.ok(PagedResponse.from(alerts, AlertResponse::fromModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        Alert alert = alertService.getAlertById(id);
        if (!accessService.hasAccess(alert.getDeviceId(), currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this alert");
        }
        return ResponseEntity.ok(ApiResponse.ok(AlertResponse.fromModel(alert)));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get alerts for a specific device (paginated)")
    public ResponseEntity<PagedResponse<AlertResponse>> getAlertsByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(deviceId, currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this device");
        }
        Page<Alert> alerts = alertService.getAlertsByDeviceId(deviceId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(PagedResponse.from(alerts, AlertResponse::fromModel));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve alert with optional note (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ResolveAlertRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        Alert alert = alertService.getAlertById(id);
        if (!accessService.hasAccess(alert.getDeviceId(), currentUser.getId(), Permission.OPERATE)) {
            throw new InvalidOperationException("You don't have permission to resolve this alert");
        }
        AlertResponse response = AlertResponse.fromModel(
                alertService.resolveAlert(id, request, currentUser.getId()));
        return ResponseEntity.ok(ApiResponse.ok(response, "Alert resolved"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete alert (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(@PathVariable UUID id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Alert deleted"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get alert statistics")
    public ResponseEntity<ApiResponse<AlertStatsResponse>> getAlertStats(
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(currentUser.getId());
        AlertStatsResponse stats = alertService.getAlertStats(deviceIds);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}