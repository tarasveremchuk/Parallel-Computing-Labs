package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.dto.response.PagedResponse;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.repository.AlertRepository;
import com.iotplatform.telemetry.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.function.Function;

@RestController @RequestMapping("/v1/alerts") @RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final DeviceServiceClient deviceServiceClient;

    @GetMapping
    public ResponseEntity<PagedResponse<Alert>> getAll(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) Boolean unresolvedOnly,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "desc") String direction,
            Authentication auth) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.ok(PagedResponse.from(alertService.getAll(severity, unresolvedOnly, pageable), Function.identity()));
        }

        List<UUID> accessibleIds = getAccessibleIds(auth);
        if (accessibleIds.isEmpty()) {
            return ResponseEntity.ok(PagedResponse.from(new PageImpl<>(List.of(), pageable, 0), Function.identity()));
        }
        Page<Alert> alerts = alertRepository.findByDeviceIdIn(accessibleIds, pageable);
        return ResponseEntity.ok(PagedResponse.from(alerts, Function.identity()));
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

    private List<UUID> getAccessibleIds(Authentication auth) {
        try {
            String userId = (String) auth.getCredentials();
            List<String> ids = deviceServiceClient.getAccessibleDeviceIds(UUID.fromString(userId));
            return ids.stream().map(UUID::fromString).toList();
        } catch (Exception e) { return List.of(); }
    }
}