package com.iotplatform.service;

import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.MaintenanceWindow;
import com.iotplatform.repository.DeviceRepository;
import com.iotplatform.repository.MaintenanceWindowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceWindowRepository maintenanceRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public MaintenanceWindow schedule(UUID deviceId, LocalDateTime startTime,
                                      LocalDateTime endTime, String reason, UUID scheduledBy) {
        if (!deviceRepository.existsByIdAndDeletedFalse(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        if (startTime.isAfter(endTime)) {
            throw new InvalidOperationException("Start time must be before end time");
        }
        if (endTime.isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("End time must be in the future");
        }

        MaintenanceWindow window = MaintenanceWindow.builder()
                .deviceId(deviceId)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .scheduledBy(scheduledBy)
                .cancelled(false)
                .build();

        MaintenanceWindow saved = maintenanceRepository.save(window);
        log.info("Maintenance scheduled for device {} from {} to {}", deviceId, startTime, endTime);
        return saved;
    }

    @Transactional
    public MaintenanceWindow cancel(UUID id) {
        MaintenanceWindow window = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", "id", id));

        if (window.isCancelled()) {
            throw new InvalidOperationException("Maintenance window is already cancelled");
        }

        window.setCancelled(true);
        maintenanceRepository.save(window);
        log.info("Maintenance cancelled: {}", id);
        return window;
    }

    public boolean isUnderMaintenance(UUID deviceId) {
        List<MaintenanceWindow> active = maintenanceRepository
                .findActiveByDeviceId(deviceId, LocalDateTime.now());
        return !active.isEmpty();
    }

    public Page<MaintenanceWindow> getByDeviceId(UUID deviceId, Pageable pageable) {
        return maintenanceRepository.findByDeviceIdAndCancelledFalse(deviceId, pageable);
    }

    public Page<MaintenanceWindow> getAll(Pageable pageable) {
        return maintenanceRepository.findByCancelledFalse(pageable);
    }

    public List<MaintenanceWindow> getUpcoming(UUID deviceId) {
        return maintenanceRepository.findUpcomingByDeviceId(deviceId, LocalDateTime.now());
    }

    public List<MaintenanceWindow> getAllActive() {
        return maintenanceRepository.findAllActive(LocalDateTime.now());
    }
}