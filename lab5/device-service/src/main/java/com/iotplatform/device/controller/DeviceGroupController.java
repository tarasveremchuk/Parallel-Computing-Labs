package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.exception.DuplicateResourceException;
import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.DeviceGroup;
import com.iotplatform.device.model.DeviceGroupMembership;
import com.iotplatform.device.repository.DeviceGroupMembershipRepository;
import com.iotplatform.device.repository.DeviceGroupRepository;
import com.iotplatform.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/v1/groups") @RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupRepository groupRepository;
    private final DeviceGroupMembershipRepository membershipRepository;
    private final DeviceRepository deviceRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceGroup>> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (groupRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Group '" + name + "' already exists");
        }
        DeviceGroup group = DeviceGroup.builder()
                .name(name)
                .description(body.getOrDefault("description", null))
                .color(body.getOrDefault("color", "#10b981"))
                .createdBy(UUID.fromString(body.getOrDefault("createdBy", "00000000-0000-0000-0000-000000000001")))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(groupRepository.save(group)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceGroup>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(groupRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable UUID id) {
        DeviceGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceGroup", "id", id));
        List<UUID> deviceIds = membershipRepository.findDeviceIdsByGroupId(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("group", group);
        result.put("deviceCount", deviceIds.size());
        result.put("deviceIds", deviceIds);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        DeviceGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceGroup", "id", id));
        membershipRepository.deleteByGroupId(id);
        groupRepository.delete(group);
        return ResponseEntity.ok(ApiResponse.ok(null, "Group deleted"));
    }

    @PostMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> addDevice(@PathVariable UUID groupId, @PathVariable UUID deviceId) {
        if (!groupRepository.existsById(groupId)) throw new ResourceNotFoundException("DeviceGroup", "id", groupId);
        if (!deviceRepository.existsByIdAndDeletedFalse(deviceId)) throw new ResourceNotFoundException("Device", "id", deviceId);
        if (membershipRepository.existsByGroupIdAndDeviceId(groupId, deviceId)) throw new DuplicateResourceException("Device already in group");
        membershipRepository.save(DeviceGroupMembership.builder().groupId(groupId).deviceId(deviceId).build());
        return ResponseEntity.ok(ApiResponse.ok(null, "Device added"));
    }

    @DeleteMapping("/{groupId}/devices/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> removeDevice(@PathVariable UUID groupId, @PathVariable UUID deviceId) {
        membershipRepository.deleteByGroupIdAndDeviceId(groupId, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Device removed"));
    }
}