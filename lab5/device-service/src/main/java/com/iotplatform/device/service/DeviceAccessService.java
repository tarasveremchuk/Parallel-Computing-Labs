package com.iotplatform.device.service;

import com.iotplatform.device.exception.DuplicateResourceException;
import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.DeviceAccess;
import com.iotplatform.device.model.enums.Permission;
import com.iotplatform.device.repository.DeviceAccessRepository;
import com.iotplatform.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class DeviceAccessService {

    private final DeviceAccessRepository accessRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceAccess grantAccess(UUID deviceId, UUID userId, Permission permission) {
        if (accessRepository.findByDeviceIdAndUserId(deviceId, userId).isPresent()) {
            throw new DuplicateResourceException("User already has access to this device");
        }
        DeviceAccess access = DeviceAccess.builder()
                .deviceId(deviceId).userId(userId).permission(permission).build();
        DeviceAccess saved = accessRepository.save(access);
        log.info("Access granted: user {} → device {} [{}]", userId, deviceId, permission);
        return saved;
    }

    public List<DeviceAccess> getAccessByDevice(UUID deviceId) {
        return accessRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public DeviceAccess updateAccess(UUID deviceId, UUID userId, Permission permission) {
        DeviceAccess access = accessRepository.findByDeviceIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceAccess", "deviceId+userId", deviceId + "/" + userId));
        access.setPermission(permission);
        accessRepository.save(access);
        return access;
    }

    @Transactional
    public void revokeAccess(UUID deviceId, UUID userId) {
        accessRepository.deleteByDeviceIdAndUserId(deviceId, userId);
        log.info("Access revoked: user {} from device {}", userId, deviceId);
    }

    public boolean hasAccess(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findByIdAndDeletedFalse(deviceId).orElse(null);
        if (device != null && device.getOwnerId().equals(userId)) return true;
        return accessRepository.findByDeviceIdAndUserId(deviceId, userId).isPresent();
    }

    public List<UUID> getAccessibleDeviceIds(UUID userId) {
        List<UUID> owned = deviceRepository.findByDeletedFalse(null).stream()
                .filter(d -> d.getOwnerId().equals(userId)).map(Device::getId).toList();
        List<UUID> shared = accessRepository.findDeviceIdsByUserId(userId);
        return java.util.stream.Stream.concat(owned.stream(), shared.stream()).distinct().toList();
    }
}