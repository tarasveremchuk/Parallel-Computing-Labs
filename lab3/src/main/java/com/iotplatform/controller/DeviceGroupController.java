package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.model.DeviceGroup;
import com.iotplatform.model.DeviceGroupMembership;
import com.iotplatform.model.User;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Device Groups", description = "Organize devices into groups")
public class DeviceGroupController {

    private final DeviceGroupService groupService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a device group")
    public ResponseEntity<ApiResponse<DeviceGroup>> createGroup(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        DeviceGroup group = groupService.createGroup(
                body.get("name"),
                body.getOrDefault("description", null),
                body.getOrDefault("color", "#10b981"),
                user.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(group));
    }

    @GetMapping
    @Operation(summary = "Get all device groups")
    public ResponseEntity<ApiResponse<List<DeviceGroup>>> getAllGroups() {
        List<DeviceGroup> groups = groupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.ok(groups, "Found " + groups.size() + " group(s)"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID with device count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGroupById(@PathVariable UUID id) {
        DeviceGroup group = groupService.getGroupById(id);
        long deviceCount = groupService.getDeviceCount(id);
        List<UUID> deviceIds = groupService.getDeviceIdsInGroup(id);

        Map<String, Object> result = Map.of(
                "group", group,
                "deviceCount", deviceCount,
                "deviceIds", deviceIds
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device group")
    public ResponseEntity<ApiResponse<DeviceGroup>> updateGroup(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        DeviceGroup group = groupService.updateGroup(id,
                body.get("name"),
                body.get("description"),
                body.get("color")
        );
        return ResponseEntity.ok(ApiResponse.ok(group, "Group updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device group")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Group deleted"));
    }

    @PostMapping("/{groupId}/devices/{deviceId}")
    @Operation(summary = "Add device to group")
    public ResponseEntity<ApiResponse<Void>> addDevice(
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId) {
        groupService.addDeviceToGroup(groupId, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Device added to group"));
    }

    @DeleteMapping("/{groupId}/devices/{deviceId}")
    @Operation(summary = "Remove device from group")
    public ResponseEntity<ApiResponse<Void>> removeDevice(
            @PathVariable UUID groupId,
            @PathVariable UUID deviceId) {
        groupService.removeDeviceFromGroup(groupId, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Device removed from group"));
    }

    @GetMapping("/{groupId}/devices")
    @Operation(summary = "Get devices in group")
    public ResponseEntity<ApiResponse<List<DeviceGroupMembership>>> getGroupDevices(
            @PathVariable UUID groupId) {
        List<DeviceGroupMembership> memberships = groupService.getGroupMemberships(groupId);
        return ResponseEntity.ok(ApiResponse.ok(memberships));
    }
}