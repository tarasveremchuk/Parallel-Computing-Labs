package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.dto.response.PagedResponse;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/alerts") @RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<PagedResponse<Alert>> getAll(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) Boolean unresolvedOnly,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(PagedResponse.from(alertService.getAll(severity, unresolvedOnly, PageRequest.of(page, size, sort)), Function.identity()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Alert>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getById(id)));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<PagedResponse<Alert>> getByDevice(@PathVariable UUID deviceId,
                                                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(alertService.getByDevice(deviceId, PageRequest.of(page, size, Sort.by("createdAt").descending())), Function.identity()));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Alert>> resolve(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(ApiResponse.ok(alertService.resolve(id, note, null), "Alert resolved"));
    }
}