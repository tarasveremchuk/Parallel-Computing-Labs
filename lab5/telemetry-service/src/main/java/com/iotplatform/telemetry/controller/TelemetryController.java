package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.dto.response.PagedResponse;
import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.enums.MetricType;
import com.iotplatform.telemetry.repository.TelemetryRepository;
import com.iotplatform.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.function.Function;

@RestController @RequestMapping("/v1/telemetry") @RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;
    private final TelemetryRepository telemetryRepository;
    private final DeviceServiceClient deviceServiceClient;

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
            @RequestParam(defaultValue = "timestamp") String sortBy, @RequestParam(defaultValue = "desc") String direction,
            Authentication auth) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.ok(PagedResponse.from(telemetryService.getAll(pageable), Function.identity()));
        }

        List<UUID> accessibleIds = getAccessibleIds(auth);
        if (accessibleIds.isEmpty()) {
            return ResponseEntity.ok(PagedResponse.from(new PageImpl<>(List.of(), pageable, 0), Function.identity()));
        }
        Page<TelemetryReading> readings = telemetryRepository.findByDeviceIdIn(accessibleIds, pageable);
        return ResponseEntity.ok(PagedResponse.from(readings, Function.identity()));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<PagedResponse<TelemetryReading>> getByDevice(
            @PathVariable UUID deviceId, @RequestParam(required = false) MetricType metricType,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy, @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(PagedResponse.from(telemetryService.getByDevice(deviceId, metricType, PageRequest.of(page, size, sort)), Function.identity()));
    }

    @GetMapping("/device/{deviceId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeviceStats(@PathVariable UUID deviceId) {
        long total = telemetryService.countByDevice(deviceId);
        long anomalies = telemetryService.countAnomaliesByDevice(deviceId);
        int healthScore = total > 0 ? (int) Math.round((1.0 - (double) anomalies / total) * 100) : 100;
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalReadings", total);
        stats.put("anomalyCount", anomalies);
        stats.put("healthScore", healthScore);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    private List<UUID> getAccessibleIds(Authentication auth) {
        try {
            String userId = (String) auth.getCredentials();
            List<String> ids = deviceServiceClient.getAccessibleDeviceIds(UUID.fromString(userId));
            return ids.stream().map(UUID::fromString).toList();
        } catch (Exception e) { return List.of(); }
    }
}