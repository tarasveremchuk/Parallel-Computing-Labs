package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.repository.AlertRepository;
import com.iotplatform.telemetry.repository.TelemetryRepository;
import com.iotplatform.telemetry.service.AlertService;
import com.iotplatform.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@Slf4j @RestController @RequestMapping("/v1/dashboard") @RequiredArgsConstructor
public class DashboardController {

    private final TelemetryService telemetryService;
    private final AlertService alertService;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;
    private final DeviceServiceClient deviceServiceClient;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Authentication auth) {
        Map<String, Object> data = new LinkedHashMap<>();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String userId = (String) auth.getCredentials();

        if (isAdmin) {
            // Admin бачить все
            try {
                data.put("totalDevices", deviceServiceClient.countAllDevices());
                data.put("onlineDevices", deviceServiceClient.countOnlineDevices());
            } catch (Exception e) {
                data.put("totalDevices", 0);
                data.put("onlineDevices", 0);
            }

            long totalReadings = telemetryService.count();
            long anomalies = telemetryService.countAnomalies();
            data.put("totalReadings", totalReadings);
            data.put("anomalyRate", totalReadings > 0 ? Math.round((double) anomalies / totalReadings * 100) : 0);
            data.put("unresolvedAlerts", alertService.countUnresolved());
            data.put("totalAlerts", alertService.countUnresolved());

            Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
            for (AlertSeverity s : AlertSeverity.values()) {
                alertsBySeverity.put(s.name(), alertService.countBySeverity(s));
            }
            data.put("alertsBySeverity", alertsBySeverity);
        } else {
            // Non-admin: тільки свої пристрої
            List<UUID> accessibleIds = new ArrayList<>();
            try {
                List<String> ids = deviceServiceClient.getAccessibleDeviceIds(UUID.fromString(userId));
                for (String id : ids) accessibleIds.add(UUID.fromString(id));
            } catch (Exception e) {
                log.warn("Could not fetch accessible devices: {}", e.getMessage());
            }

            data.put("totalDevices", accessibleIds.size());

            // Count online among accessible
            long onlineCount = 0;
            try {
                for (UUID devId : accessibleIds) {
                    Map<String, Object> dev = deviceServiceClient.getDevice(devId);
                    if ("ONLINE".equals(dev.get("status"))) onlineCount++;
                }
            } catch (Exception e) { log.warn("Error counting online: {}", e.getMessage()); }
            data.put("onlineDevices", onlineCount);

            // Telemetry stats for accessible devices only
            long totalReadings = 0;
            long anomalies = 0;
            long unresolvedAlerts = 0;
            Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
            for (AlertSeverity s : AlertSeverity.values()) alertsBySeverity.put(s.name(), 0L);

            for (UUID devId : accessibleIds) {
                totalReadings += telemetryRepository.countByDeviceId(devId);
                anomalies += telemetryRepository.countByDeviceIdAndAnomalyTrue(devId);
                unresolvedAlerts += alertRepository.countByDeviceIdAndResolvedFalse(devId);
                for (AlertSeverity s : AlertSeverity.values()) {
                    // Simple count - we'll use total for now
                }
            }

            data.put("totalReadings", totalReadings);
            data.put("anomalyRate", totalReadings > 0 ? Math.round((double) anomalies / totalReadings * 100) : 0);
            data.put("unresolvedAlerts", unresolvedAlerts);
            data.put("totalAlerts", unresolvedAlerts);
            data.put("alertsBySeverity", alertsBySeverity);
        }

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}