package com.iotplatform.controller;

import com.iotplatform.dto.request.BatchTelemetryRequest;
import com.iotplatform.dto.request.CreateTelemetryRequest;
import com.iotplatform.dto.response.*;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.model.enums.Permission;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceAccessService;
import com.iotplatform.service.TelemetryService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetry", description = "Telemetry data ingestion and retrieval")
public class TelemetryController {

    private final TelemetryService telemetryService;
    private final AuthService authService;
    private final DeviceAccessService accessService;

    @PostMapping
    @Operation(summary = "Ingest single telemetry reading (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<TelemetryResponse>> ingestTelemetry(
            @Valid @RequestBody CreateTelemetryRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(request.getDeviceId(), currentUser.getId(), Permission.OPERATE)) {
            throw new InvalidOperationException("You don't have permission to send telemetry for this device");
        }
        TelemetryResponse response = TelemetryResponse.fromModel(
                telemetryService.ingestTelemetry(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/batch")
    @Operation(summary = "Ingest batch of telemetry readings (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<BatchTelemetryResponse>> ingestBatch(
            @Valid @RequestBody BatchTelemetryRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(request.getDeviceId(), currentUser.getId(), Permission.OPERATE)) {
            throw new InvalidOperationException("You don't have permission to send telemetry for this device");
        }
        BatchTelemetryResponse response = telemetryService.ingestBatch(request);
        return ResponseEntity.ok(ApiResponse.ok(response,
                "Batch ingested: " + response.getTotalReceived() + " readings, "
                        + response.getAnomalyCount() + " anomalies"));
    }

    @GetMapping
    @Operation(summary = "Get telemetry readings (paginated, accessible devices only)")
    public ResponseEntity<PagedResponse<TelemetryResponse>> getAllReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(currentUser.getId());

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<TelemetryReading> readings = telemetryService.getReadingsByAccessibleDevices(
                deviceIds, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(PagedResponse.from(readings, TelemetryResponse::fromModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get telemetry reading by ID")
    public ResponseEntity<ApiResponse<TelemetryResponse>> getReadingById(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        TelemetryReading reading = telemetryService.getReadingById(id);
        if (!accessService.hasAccess(reading.getDeviceId(), currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this device's telemetry");
        }
        return ResponseEntity.ok(ApiResponse.ok(TelemetryResponse.fromModel(reading)));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get readings by device with optional metric filter (paginated)")
    public ResponseEntity<PagedResponse<TelemetryResponse>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) MetricType metricType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        if (!accessService.hasAccess(deviceId, currentUser.getId(), Permission.READ)) {
            throw new InvalidOperationException("You don't have access to this device");
        }

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<TelemetryReading> readings;
        if (metricType != null) {
            readings = telemetryService.getReadingsByDeviceAndMetric(deviceId, metricType, pageable);
        } else {
            readings = telemetryService.getReadingsByDeviceId(deviceId, pageable);
        }

        return ResponseEntity.ok(PagedResponse.from(readings, TelemetryResponse::fromModel));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get anomalous readings (paginated, accessible devices only)")
    public ResponseEntity<PagedResponse<TelemetryResponse>> getAnomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(currentUser.getId());

        Page<TelemetryReading> anomalies = telemetryService.getAnomaliesByAccessibleDevices(
                deviceIds, PageRequest.of(page, size, Sort.by("timestamp").descending()));

        return ResponseEntity.ok(PagedResponse.from(anomalies, TelemetryResponse::fromModel));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete telemetry reading (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteReading(@PathVariable UUID id) {
        telemetryService.deleteReading(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Telemetry reading deleted"));
    }
}