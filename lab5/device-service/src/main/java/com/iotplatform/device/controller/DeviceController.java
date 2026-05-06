package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.dto.response.PagedResponse;
import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import com.iotplatform.device.repository.DeviceAccessRepository;
import com.iotplatform.device.repository.DeviceRepository;
import com.iotplatform.device.service.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Stream;

@RestController @RequestMapping("/v1/devices") @RequiredArgsConstructor
@Tag(name = "Devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final DeviceAccessRepository accessRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Device>> create(@RequestBody Map<String, String> body, Authentication auth) {
        UUID ownerId = UUID.fromString((String) auth.getCredentials());
        Device device = deviceService.createDevice(
                body.get("name"), DeviceType.valueOf(body.get("type")),
                body.get("location"), body.getOrDefault("firmwareVersion", null),
                ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(device));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Device>> getAll(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication auth) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            Page<Device> devices = deviceService.getAllDevices(status, type, pageable);
            return ResponseEntity.ok(PagedResponse.from(devices, Function.identity()));
        }

        // Non-admin: show only owned + shared devices
        UUID userId = UUID.fromString((String) auth.getCredentials());
        List<Device> ownedDevices = deviceRepository.findByOwnerIdAndDeletedFalse(userId);
        List<UUID> sharedDeviceIds = accessRepository.findDeviceIdsByUserId(userId);

        List<Device> allAccessible = new ArrayList<>(ownedDevices);
        for (UUID sharedId : sharedDeviceIds) {
            if (allAccessible.stream().noneMatch(d -> d.getId().equals(sharedId))) {
                deviceRepository.findByIdAndDeletedFalse(sharedId).ifPresent(allAccessible::add);
            }
        }

        // Apply filters
        if (status != null) allAccessible.removeIf(d -> d.getStatus() != status);
        if (type != null) allAccessible.removeIf(d -> d.getType() != type);

        // Sort
        allAccessible.sort((a, b) -> {
            if (sortBy.equals("name")) return direction.equals("asc") ?
                    a.getName().compareTo(b.getName()) : b.getName().compareTo(a.getName());
            return direction.equals("asc") ?
                    a.getCreatedAt().compareTo(b.getCreatedAt()) : b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        int start = Math.min(page * size, allAccessible.size());
        int end = Math.min(start + size, allAccessible.size());
        Page<Device> pagedResult = new PageImpl<>(allAccessible.subList(start, end), pageable, allAccessible.size());

        return ResponseEntity.ok(PagedResponse.from(pagedResult, Function.identity()));
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