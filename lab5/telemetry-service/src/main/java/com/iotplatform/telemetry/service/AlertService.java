package com.iotplatform.telemetry.service;

import com.iotplatform.telemetry.exception.InvalidOperationException;
import com.iotplatform.telemetry.exception.ResourceNotFoundException;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    public Alert createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        log.warn("ALERT created: {} [{}] on device {}", alert.getMetricType(), alert.getSeverity(), alert.getDeviceId());
        return saved;
    }

    public Page<Alert> getAll(AlertSeverity severity, Boolean unresolvedOnly, Pageable pageable) {
        if (unresolvedOnly != null && unresolvedOnly) return alertRepository.findByResolvedFalse(pageable);
        if (severity != null) return alertRepository.findBySeverity(severity, pageable);
        return alertRepository.findAll(pageable);
    }

    public Alert getById(UUID id) {
        return alertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }

    public Page<Alert> getByDevice(UUID deviceId, Pageable pageable) {
        return alertRepository.findByDeviceId(deviceId, pageable);
    }

    @Transactional
    public Alert resolve(UUID id, String note, UUID resolvedBy) {
        Alert alert = getById(id);
        if (alert.isResolved()) throw new InvalidOperationException("Alert is already resolved");
        alert.setResolved(true);
        alert.setResolutionNote(note);
        alert.setResolvedBy(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
        log.info("Alert {} resolved", id);
        return alert;
    }

    public long countUnresolved() { return alertRepository.countByResolvedFalse(); }
    public long countBySeverity(AlertSeverity severity) { return alertRepository.countBySeverity(severity); }
}