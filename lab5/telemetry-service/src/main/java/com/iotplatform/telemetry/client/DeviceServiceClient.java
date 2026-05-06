package com.iotplatform.telemetry.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "device-service", url = "${services.device-service.url}")
public interface DeviceServiceClient {

    @GetMapping("/v1/internal/devices/{id}/exists")
    Boolean deviceExists(@PathVariable UUID id);

    @GetMapping("/v1/internal/devices/{id}")
    Map<String, Object> getDevice(@PathVariable UUID id);

    @GetMapping("/v1/internal/devices")
    List<Map<String, Object>> getAllDevices();
    @GetMapping("/v1/internal/devices/accessible/{userId}")
    List<String> getAccessibleDeviceIds(@PathVariable UUID userId);
    @GetMapping("/v1/internal/rules/applicable")
    List<Map<String, Object>> getApplicableRules(@RequestParam UUID deviceId, @RequestParam String metricType);

    @GetMapping("/v1/internal/maintenance/{deviceId}/active")
    Boolean isUnderMaintenance(@PathVariable UUID deviceId);

    @GetMapping("/v1/internal/devices/count")
    Long countAllDevices();

    @GetMapping("/v1/internal/devices/count/online")
    Long countOnlineDevices();
}