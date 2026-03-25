package com.iotplatform.service;

import com.iotplatform.dto.request.CreateDeviceRequest;
import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Device;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import com.iotplatform.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public Device registerDevice(CreateDeviceRequest request) {
        boolean nameExists = deviceRepository.findAll().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase(request.getName()));
        if (nameExists) {
            throw new DuplicateResourceException(
                    "Device with name '" + request.getName() + "' already exists");
        }

        Device device = Device.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .type(request.getType())
                .status(DeviceStatus.ONLINE)
                .location(request.getLocation())
                .firmwareVersion(request.getFirmwareVersion() != null
                        ? request.getFirmwareVersion() : "1.0.0")
                .registeredAt(LocalDateTime.now())
                .lastSeenAt(LocalDateTime.now())
                .build();

        Device saved = deviceRepository.save(device);
        log.info("Device registered: {} [{}] at {}", saved.getName(), saved.getId(), saved.getLocation());
        return saved;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
    }

    public List<Device> getDevicesByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }

    public List<Device> getDevicesByType(DeviceType type) {
        return deviceRepository.findByType(type);
    }

    public List<Device> searchByLocation(String location) {
        return deviceRepository.findByLocation(location);
    }

    public boolean existsById(UUID id) {
        return deviceRepository.existsById(id);
    }

    public long getDeviceCount() {
        return deviceRepository.count();
    }
    public Device heartbeat(UUID id) {
        Device device = getDeviceById(id);
        device.setLastSeenAt(LocalDateTime.now());
        if (device.getStatus() == DeviceStatus.OFFLINE || device.getStatus() == DeviceStatus.ERROR) {
            device.setStatus(DeviceStatus.ONLINE);
            log.info("Device back online: {} [{}]", device.getName(), device.getId());
        }
        deviceRepository.save(device);
        log.debug("Heartbeat received from device: {} [{}]", device.getName(), device.getId());
        return device;
    }

    public Device updateStatus(UUID id, DeviceStatus status) {
        Device device = getDeviceById(id);
        DeviceStatus oldStatus = device.getStatus();
        device.setStatus(status);
        deviceRepository.save(device);
        log.info("Device status changed: {} [{}] {} -> {}", device.getName(), device.getId(), oldStatus, status);
        return device;
    }
}