package com.iotplatform.service;

import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    public Alert createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        log.info("Alert created: [{}] {} on device {}",
                saved.getSeverity(), saved.getMetricType(), saved.getDeviceId());
        return saved;
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public Alert getAlertById(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }

    public List<Alert> getAlertsByDeviceId(UUID deviceId) {
        return alertRepository.findByDeviceId(deviceId);
    }

    public List<Alert> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    public List<Alert> getUnresolvedAlerts() {
        return alertRepository.findUnresolved();
    }

    public Alert resolveAlert(UUID id) {
        Alert alert = getAlertById(id);
        if (alert.isResolved()) {
            throw new InvalidOperationException("Alert is already resolved");
        }
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
        log.info("Alert resolved: {}", id);
        return alert;
    }

    public long getUnresolvedCount() {
        return alertRepository.countUnresolved();
    }
}