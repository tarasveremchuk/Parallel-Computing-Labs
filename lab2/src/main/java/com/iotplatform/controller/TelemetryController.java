package com.iotplatform.controller;

import com.iotplatform.dto.request.CreateTelemetryRequest;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.TelemetryResponse;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.service.TelemetryService;
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
@RequestMapping("/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetry", description = "Telemetry data ingestion and retrieval")
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    @Operation(summary = "Ingest telemetry reading — triggers anomaly detection (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<TelemetryResponse>> ingestTelemetry(
            @Valid @RequestBody CreateTelemetryRequest request) {
        TelemetryResponse response = TelemetryResponse.fromModel(
                telemetryService.ingestTelemetry(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "Get all telemetry readings")
    public ResponseEntity<ApiResponse<List<TelemetryResponse>>> getAllReadings() {
        List<TelemetryResponse> readings = telemetryService.getAllReadings().stream()
                .map(TelemetryResponse::fromModel).toList();
        return ResponseEntity.ok(ApiResponse.ok(readings,
                "Found " + readings.size() + " reading(s)"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get telemetry reading by ID")
    public ResponseEntity<ApiResponse<TelemetryResponse>> getReadingById(@PathVariable UUID id) {
        TelemetryResponse response = TelemetryResponse.fromModel(
                telemetryService.getReadingById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get readings by device with optional metric filter")
    public ResponseEntity<ApiResponse<List<TelemetryResponse>>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) MetricType metricType) {

        List<TelemetryResponse> readings;
        if (metricType != null) {
            readings = telemetryService.getReadingsByDeviceAndMetric(deviceId, metricType).stream()
                    .map(TelemetryResponse::fromModel).toList();
        } else {
            readings = telemetryService.getReadingsByDeviceId(deviceId).stream()
                    .map(TelemetryResponse::fromModel).toList();
        }

        return ResponseEntity.ok(ApiResponse.ok(readings,
                "Found " + readings.size() + " reading(s) for device " + deviceId));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get all anomalous telemetry readings")
    public ResponseEntity<ApiResponse<List<TelemetryResponse>>> getAnomalies() {
        List<TelemetryResponse> anomalies = telemetryService.getAnomalies().stream()
                .map(TelemetryResponse::fromModel).toList();
        return ResponseEntity.ok(ApiResponse.ok(anomalies,
                "Found " + anomalies.size() + " anomaly(ies)"));
    }
}