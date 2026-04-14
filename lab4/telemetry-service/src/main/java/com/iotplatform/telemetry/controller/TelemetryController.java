package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.dto.response.PagedResponse;
import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.enums.MetricType;
import com.iotplatform.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/telemetry") @RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    public ResponseEntity<ApiResponse<TelemetryReading>> ingest(@RequestBody Map<String, Object> body) {
        TelemetryReading reading = telemetryService.ingest(
                UUID.fromString((String) body.get("deviceId")),
                MetricType.valueOf((String) body.get("metricType")),
                ((Number) body.get("value")).doubleValue(),
                (String) body.getOrDefault("unit", null));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(reading));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TelemetryReading>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy, @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(PagedResponse.from(telemetryService.getAll(PageRequest.of(page, size, sort)), Function.identity()));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<PagedResponse<TelemetryReading>> getByDevice(
            @PathVariable UUID deviceId, @RequestParam(required = false) MetricType metricType,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy, @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(PagedResponse.from(telemetryService.getByDevice(deviceId, metricType, PageRequest.of(page, size, sort)), Function.identity()));
    }
}