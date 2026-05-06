package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.dto.response.PagedResponse;
import com.iotplatform.device.model.MaintenanceWindow;
import com.iotplatform.device.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/maintenance") @RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceWindow>> schedule(@RequestBody Map<String, String> body) {
        MaintenanceWindow window = maintenanceService.schedule(
                UUID.fromString(body.get("deviceId")),
                LocalDateTime.parse(body.get("startTime")),
                LocalDateTime.parse(body.get("endTime")),
                body.getOrDefault("reason", null),
                UUID.fromString(body.getOrDefault("scheduledBy", "00000000-0000-0000-0000-000000000001")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(window));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<MaintenanceWindow>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(maintenanceService.cancel(id), "Cancelled"));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<MaintenanceWindow>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                maintenanceService.getAll(PageRequest.of(page, size, Sort.by("startTime").descending())),
                Function.identity()));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<PagedResponse<MaintenanceWindow>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                maintenanceService.getByDevice(deviceId, PageRequest.of(page, size, Sort.by("startTime").descending())),
                Function.identity()));
    }
}