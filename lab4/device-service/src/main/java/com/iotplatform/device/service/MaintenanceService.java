package com.iotplatform.device.service;

import com.iotplatform.device.exception.InvalidOperationException;
import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.MaintenanceWindow;
import com.iotplatform.device.repository.MaintenanceWindowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceWindowRepository maintenanceRepository;

    @Transactional
    public MaintenanceWindow schedule(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime, String reason, UUID scheduledBy) {
        if (startTime.isAfter(endTime)) throw new InvalidOperationException("Start must be before end");
        MaintenanceWindow window = MaintenanceWindow.builder()
                .deviceId(deviceId).startTime(startTime).endTime(endTime)
                .reason(reason).scheduledBy(scheduledBy).cancelled(false).build();
        return maintenanceRepository.save(window);
    }

    @Transactional
    public MaintenanceWindow cancel(UUID id) {
        MaintenanceWindow w = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", "id", id));
        w.setCancelled(true);
        return maintenanceRepository.save(w);
    }

    public boolean isUnderMaintenance(UUID deviceId) {
        return !maintenanceRepository.findActiveByDeviceId(deviceId, LocalDateTime.now()).isEmpty();
    }

    public Page<MaintenanceWindow> getByDevice(UUID deviceId, Pageable pageable) {
        return maintenanceRepository.findByDeviceIdAndCancelledFalse(deviceId, pageable);
    }

    public Page<MaintenanceWindow> getAll(Pageable pageable) {
        return maintenanceRepository.findByCancelledFalse(pageable);
    }
}