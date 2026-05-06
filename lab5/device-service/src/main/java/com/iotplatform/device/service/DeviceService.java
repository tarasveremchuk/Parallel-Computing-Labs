package com.iotplatform.device.service;

import com.iotplatform.device.exception.DuplicateResourceException;
import com.iotplatform.device.exception.InvalidOperationException;
import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import com.iotplatform.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Device createDevice(String name, DeviceType type, String location, String firmwareVersion, UUID ownerId) {
        Device device = Device.builder()
                .name(name).type(type).status(DeviceStatus.OFFLINE)
                .location(location).firmwareVersion(firmwareVersion)
                .ownerId(ownerId).deleted(false).build();
        Device saved = deviceRepository.save(device);
        log.info("Device created: {} [{}]", saved.getName(), saved.getId());
        return saved;
    }

    public Page<Device> getAllDevices(DeviceStatus status, DeviceType type, Pageable pageable) {
        if (status != null && type != null) return deviceRepository.findByDeletedFalseAndStatusAndType(status, type, pageable);
        if (status != null) return deviceRepository.findByDeletedFalseAndStatus(status, pageable);
        if (type != null) return deviceRepository.findByDeletedFalseAndType(type, pageable);
        return deviceRepository.findByDeletedFalse(pageable);
    }

    @Cacheable(cacheNames = "device", key = "#id")
    public Device getDeviceById(UUID id) {
        return deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
    }

    @Transactional
    @CacheEvict(cacheNames = "device", key = "#id")
    public Device updateDevice(UUID id, String name, DeviceType type, String location, String firmwareVersion) {
        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        if (name != null) device.setName(name);
        if (type != null) device.setType(type);
        if (location != null) device.setLocation(location);
        if (firmwareVersion != null) device.setFirmwareVersion(firmwareVersion);
        deviceRepository.save(device);
        log.info("Device updated: {}", id);
        return device;
    }

    @Transactional
    @CacheEvict(cacheNames = "device", key = "#id")
    public void deleteDevice(UUID id) {
        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        device.setDeleted(true);
        deviceRepository.save(device);
        log.info("Device soft-deleted: {}", id);
    }

    @Transactional
    @CacheEvict(cacheNames = "device", key = "#id")
    public Device heartbeat(UUID id) {
        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        device.setLastSeenAt(LocalDateTime.now());
        device.setStatus(DeviceStatus.ONLINE);
        deviceRepository.save(device);
        return device;
    }

    @Transactional
    @CacheEvict(cacheNames = "device", key = "#id")
    public Device updateStatus(UUID id, DeviceStatus status) {
        Device device = deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        device.setStatus(status);
        deviceRepository.save(device);
        log.info("Device {} status → {}", id, status);
        return device;
    }

    public boolean existsById(UUID id) {
        return deviceRepository.existsByIdAndDeletedFalse(id);
    }

    public long countByStatus(DeviceStatus status) {
        return deviceRepository.countByDeletedFalseAndStatus(status);
    }

    public long countAll() {
        return deviceRepository.countByDeletedFalse();
    }
}