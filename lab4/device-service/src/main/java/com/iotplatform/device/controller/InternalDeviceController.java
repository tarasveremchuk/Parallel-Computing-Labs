package com.iotplatform.device.controller;

import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.ThresholdRule;
import com.iotplatform.device.model.enums.MetricType;
import com.iotplatform.device.service.DeviceService;
import com.iotplatform.device.service.MaintenanceService;
import com.iotplatform.device.service.ThresholdRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/v1/internal") @RequiredArgsConstructor
public class InternalDeviceController {

    private final DeviceService deviceService;
    private final ThresholdRuleService ruleService;
    private final MaintenanceService maintenanceService;

    @GetMapping("/devices/{id}/exists")
    public ResponseEntity<Boolean> deviceExists(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.existsById(id));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping("/rules/applicable")
    public ResponseEntity<List<ThresholdRule>> getApplicableRules(
            @RequestParam UUID deviceId, @RequestParam MetricType metricType) {
        return ResponseEntity.ok(ruleService.getApplicableRules(deviceId, metricType));
    }

    @GetMapping("/maintenance/{deviceId}/active")
    public ResponseEntity<Boolean> isUnderMaintenance(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(maintenanceService.isUnderMaintenance(deviceId));
    }
    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(
                deviceService.getAllDevices(null, null, org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("createdAt"))).getContent()
        );
    }
    @GetMapping("/devices/count")
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok(deviceService.countAll());
    }

    @GetMapping("/devices/count/online")
    public ResponseEntity<Long> countOnline() {
        return ResponseEntity.ok(deviceService.countByStatus(com.iotplatform.device.model.enums.DeviceStatus.ONLINE));
    }
}