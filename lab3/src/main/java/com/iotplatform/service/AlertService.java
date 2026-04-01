package com.iotplatform.service;

import com.iotplatform.dto.request.ResolveAlertRequest;
import com.iotplatform.dto.response.AlertStatsResponse;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public Alert createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        log.info("Alert created: [{}] {} on device {}",
                saved.getSeverity(), saved.getMetricType(), saved.getDeviceId());
        return saved;
    }

    public Page<Alert> getAllAlerts(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }

    public Alert getAlertById(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }

    public Page<Alert> getAlertsByDeviceId(UUID deviceId, Pageable pageable) {
        return alertRepository.findByDeviceId(deviceId, pageable);
    }

    public Page<Alert> getAlertsBySeverity(AlertSeverity severity, Pageable pageable) {
        return alertRepository.findBySeverity(severity, pageable);
    }

    public Page<Alert> getUnresolvedAlerts(Pageable pageable) {
        return alertRepository.findByResolvedFalse(pageable);
    }

    public Page<Alert> getAlertsByAccessibleDevices(List<UUID> deviceIds, Pageable pageable) {
        return alertRepository.findByDeviceIdIn(deviceIds, pageable);
    }

    public Page<Alert> getUnresolvedByAccessibleDevices(List<UUID> deviceIds, Pageable pageable) {
        return alertRepository.findByResolvedFalseAndDeviceIdIn(deviceIds, pageable);
    }

    public Page<Alert> getBySeverityAndAccessibleDevices(AlertSeverity severity, List<UUID> deviceIds, Pageable pageable) {
        return alertRepository.findBySeverityAndDeviceIdIn(severity, deviceIds, pageable);
    }

    @Transactional
    public Alert resolveAlert(UUID id, ResolveAlertRequest request, UUID resolvedByUserId) {
        Alert alert = getAlertById(id);
        if (alert.isResolved()) {
            throw new InvalidOperationException("Alert is already resolved");
        }
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedByUserId);
        if (request != null && request.getNote() != null) {
            alert.setResolutionNote(request.getNote());
        }
        alertRepository.save(alert);
        log.info("Alert resolved: {} by user {} | note: {}", id, resolvedByUserId,
                request != null ? request.getNote() : "none");
        return alert;
    }

    @Transactional
    public void deleteAlert(UUID id) {
        Alert alert = getAlertById(id);
        alertRepository.delete(alert);
        log.info("Alert deleted: {}", id);
    }

    public AlertStatsResponse getAlertStats(List<UUID> deviceIds) {
        long totalAlerts = alertRepository.findByDeviceIdIn(deviceIds,
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long unresolvedAlerts = alertRepository.countByResolvedFalseAndDeviceIdIn(deviceIds);
        long resolvedAlerts = totalAlerts - unresolvedAlerts;
        double resolutionRate = totalAlerts == 0 ? 0
                : Math.round((double) resolvedAlerts / totalAlerts * 100.0 * 10) / 10.0;

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        List<Object[]> severityGroups = alertRepository.countBySeverityGrouped(deviceIds);
        for (Object[] row : severityGroups) {
            bySeverity.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Long> unresolvedByDevice = new LinkedHashMap<>();
        List<Object[]> deviceGroups = alertRepository.countUnresolvedByDeviceGrouped(deviceIds);
        for (Object[] row : deviceGroups) {
            unresolvedByDevice.put(row[0].toString(), (Long) row[1]);
        }

        return AlertStatsResponse.builder()
                .totalAlerts(totalAlerts)
                .unresolvedAlerts(unresolvedAlerts)
                .resolvedAlerts(resolvedAlerts)
                .resolutionRate(resolutionRate)
                .bySeverity(bySeverity)
                .unresolvedByDevice(unresolvedByDevice)
                .build();
    }

    public long getUnresolvedCount() {
        return alertRepository.countByResolvedFalse();
    }
}