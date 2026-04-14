package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.dto.response.PagedResponse;
import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import com.iotplatform.device.service.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/devices") @RequiredArgsConstructor
@Tag(name = "Devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<ApiResponse<Device>> create(@RequestBody Map<String, String> body) {
        Device device = deviceService.createDevice(
                body.get("name"), DeviceType.valueOf(body.get("type")),
                body.get("location"), body.getOrDefault("firmwareVersion", null),
                UUID.fromString(body.getOrDefault("ownerId", "00000000-0000-0000-0000-000000000001")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(device));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Device>> getAll(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Device> devices = deviceService.getAllDevices(status, type, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(PagedResponse.from(devices, Function.identity()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Device>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.getDeviceById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Device>> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Device device = deviceService.updateDevice(id, body.get("name"),
                body.containsKey("type") ? DeviceType.valueOf(body.get("type")) : null,
                body.get("location"), body.get("firmwareVersion"));
        return ResponseEntity.ok(ApiResponse.ok(device, "Device updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Device deleted"));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<ApiResponse<Device>> heartbeat(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.heartbeat(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Device>> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(deviceService.updateStatus(id, DeviceStatus.valueOf(body.get("status")))));
    }
}