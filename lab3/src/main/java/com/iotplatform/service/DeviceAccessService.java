package com.iotplatform.service;

import com.iotplatform.dto.request.GrantAccessRequest;
import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Device;
import com.iotplatform.model.DeviceAccess;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.Permission;
import com.iotplatform.model.enums.UserRole;
import com.iotplatform.repository.DeviceAccessRepository;
import com.iotplatform.repository.DeviceRepository;
import com.iotplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceAccessService {

    private final DeviceAccessRepository accessRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceAccess grantAccess(UUID deviceId, GrantAccessRequest request, UUID grantedByUserId) {
        // Validate device exists
        deviceRepository.findByIdAndDeletedFalse(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        // Validate target user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Check for duplicate
        if (accessRepository.existsByDeviceIdAndUserId(deviceId, request.getUserId())) {
            throw new DuplicateResourceException(
                    "User already has access to this device. Use PUT to update permission.");
        }

        DeviceAccess access = DeviceAccess.builder()
                .deviceId(deviceId)
                .userId(request.getUserId())
                .permission(request.getPermission())
                .grantedBy(grantedByUserId)
                .build();

        DeviceAccess saved = accessRepository.save(access);
        log.info("Access granted: user {} -> device {} [{}]",
                request.getUserId(), deviceId, request.getPermission());
        return saved;
    }

    public List<DeviceAccess> getDeviceAccessList(UUID deviceId) {
        deviceRepository.findByIdAndDeletedFalse(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        return accessRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public DeviceAccess updateAccess(UUID deviceId, UUID userId, Permission permission) {
        DeviceAccess access = accessRepository.findByDeviceIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceAccess", "deviceId+userId",
                        deviceId + "/" + userId));

        access.setPermission(permission);
        accessRepository.save(access);
        log.info("Access updated: user {} -> device {} [{}]", userId, deviceId, permission);
        return access;
    }

    @Transactional
    public void revokeAccess(UUID deviceId, UUID userId) {
        if (!accessRepository.existsByDeviceIdAndUserId(deviceId, userId)) {
            throw new ResourceNotFoundException("DeviceAccess", "deviceId+userId",
                    deviceId + "/" + userId);
        }
        accessRepository.deleteByDeviceIdAndUserId(deviceId, userId);
        log.info("Access revoked: user {} -> device {}", userId, deviceId);
    }

    /**
     * Checks if user has at least the specified permission level on a device.
     * Owner always has full access. ADMIN role always has full access.
     */
    public boolean hasAccess(UUID deviceId, UUID userId, Permission requiredPermission) {
        // Check if owner
        Device device = deviceRepository.findByIdAndDeletedFalse(deviceId).orElse(null);
        if (device == null) return false;
        if (device.getOwnerId().equals(userId)) return true;

        // Check if platform admin
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == UserRole.ADMIN) return true;

        // Check explicit access
        DeviceAccess access = accessRepository.findByDeviceIdAndUserId(deviceId, userId).orElse(null);
        if (access == null) return false;

        return hasPermissionLevel(access.getPermission(), requiredPermission);
    }

    /**
     * Checks if user can manage access (is owner or has MANAGE permission).
     */
    public boolean canManageAccess(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findByIdAndDeletedFalse(deviceId).orElse(null);
        if (device == null) return false;
        if (device.getOwnerId().equals(userId)) return true;

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == UserRole.ADMIN) return true;

        return hasAccess(deviceId, userId, Permission.MANAGE);
    }

    /**
     * Get all device IDs accessible by a user (owned + shared).
     */
    public List<UUID> getAccessibleDeviceIds(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == UserRole.ADMIN) {
            return deviceRepository.findAll().stream()
                    .filter(d -> !d.isDeleted())
                    .map(Device::getId)
                    .toList();
        }

        List<UUID> ownedIds = deviceRepository.findByOwnerIdAndDeletedFalse(userId,
                        org.springframework.data.domain.Pageable.unpaged())
                .map(Device::getId)
                .toList();
        List<UUID> sharedIds = accessRepository.findDeviceIdsByUserId(userId);

        List<UUID> allIds = new java.util.ArrayList<>(ownedIds);
        sharedIds.stream().filter(id -> !allIds.contains(id)).forEach(allIds::add);
        return allIds;
    }

    private boolean hasPermissionLevel(Permission actual, Permission required) {
        int actualLevel = permissionLevel(actual);
        int requiredLevel = permissionLevel(required);
        return actualLevel >= requiredLevel;
    }

    private int permissionLevel(Permission p) {
        return switch (p) {
            case READ -> 1;
            case OPERATE -> 2;
            case MANAGE -> 3;
        };
    }
}