package com.iotplatform.service;

import com.iotplatform.dto.request.BatchTelemetryRequest;
import com.iotplatform.dto.request.CreateTelemetryRequest;
import com.iotplatform.dto.response.BatchTelemetryResponse;
import com.iotplatform.dto.response.TelemetryResponse;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final DeviceService deviceService;
    private final ThresholdRuleService thresholdRuleService;
    private final AlertService alertService;
    private final MaintenanceService maintenanceService;
    private final WebhookService webhookService;

    @Transactional
    public TelemetryReading ingestTelemetry(CreateTelemetryRequest request) {
        if (!deviceService.existsById(request.getDeviceId())) {
            throw new ResourceNotFoundException("Device", "id", request.getDeviceId());
        }

        TelemetryReading reading = TelemetryReading.builder()
                .deviceId(request.getDeviceId())
                .metricType(request.getMetricType())
                .value(request.getValue())
                .unit(request.getUnit() != null ? request.getUnit() : getDefaultUnit(request.getMetricType()))
                .anomaly(false)
                .build();

        // Rule Engine
        // Check if device is under maintenance — skip anomaly detection
        boolean underMaintenance = maintenanceService.isUnderMaintenance(request.getDeviceId());

        if (!underMaintenance) {
            // Rule Engine
            List<ThresholdRule> rules = thresholdRuleService.getApplicableRules(
                    request.getDeviceId(), request.getMetricType());

            for (ThresholdRule rule : rules) {
                if (rule.isViolated(request.getValue())) {
                    reading.setAnomaly(true);

                    AlertSeverity severity = calculateSeverity(request.getValue(), rule);
                    String message = buildAlertMessage(request, rule);

                    Alert alert = Alert.builder()
                            .deviceId(request.getDeviceId())
                            .telemetryReadingId(reading.getId())
                            .metricType(request.getMetricType())
                            .severity(severity)
                            .message(message)
                            .actualValue(request.getValue())
                            .thresholdMin(rule.getMinValue())
                            .thresholdMax(rule.getMaxValue())
                            .resolved(false)
                            .build();

                    Alert savedAlert = alertService.createAlert(alert);

                    // Trigger webhooks
                    webhookService.triggerWebhooks(savedAlert);

                    log.warn("ANOMALY DETECTED on device {}: {} = {} (rule: {})",
                            request.getDeviceId(), request.getMetricType(),
                            request.getValue(), rule.getId());
                    break;
                }
            }
        } else {
            log.debug("Device {} is under maintenance — skipping anomaly detection", request.getDeviceId());
        }

        TelemetryReading saved = telemetryRepository.save(reading);
        log.debug("Telemetry ingested: device={}, metric={}, value={}{}",
                saved.getDeviceId(), saved.getMetricType(), saved.getValue(),
                saved.isAnomaly() ? " [ANOMALY]" : "");
        return saved;
    }

    @Transactional
    public BatchTelemetryResponse ingestBatch(BatchTelemetryRequest request) {
        if (!deviceService.existsById(request.getDeviceId())) {
            throw new ResourceNotFoundException("Device", "id", request.getDeviceId());
        }

        List<TelemetryResponse> results = new ArrayList<>();
        int anomalyCount = 0;
        int alertCount = 0;

        for (BatchTelemetryRequest.Reading r : request.getReadings()) {
            CreateTelemetryRequest single = CreateTelemetryRequest.builder()
                    .deviceId(request.getDeviceId())
                    .metricType(r.getMetricType())
                    .value(r.getValue())
                    .unit(r.getUnit())
                    .build();

            TelemetryReading saved = ingestTelemetry(single);
            results.add(TelemetryResponse.fromModel(saved));
            if (saved.isAnomaly()) {
                anomalyCount++;
                alertCount++;
            }
        }

        log.info("Batch ingested: {} readings for device {}, {} anomalies",
                results.size(), request.getDeviceId(), anomalyCount);

        return BatchTelemetryResponse.builder()
                .totalReceived(results.size())
                .successCount(results.size())
                .anomalyCount(anomalyCount)
                .alertsGenerated(alertCount)
                .readings(results)
                .build();
    }

    public Page<TelemetryReading> getAllReadings(Pageable pageable) {
        return telemetryRepository.findAll(pageable);
    }

    public TelemetryReading getReadingById(UUID id) {
        return telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TelemetryReading", "id", id));
    }

    public Page<TelemetryReading> getReadingsByDeviceId(UUID deviceId, Pageable pageable) {
        if (!deviceService.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        return telemetryRepository.findByDeviceId(deviceId, pageable);
    }

    public Page<TelemetryReading> getReadingsByDeviceAndMetric(UUID deviceId, MetricType metricType, Pageable pageable) {
        if (!deviceService.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        return telemetryRepository.findByDeviceIdAndMetricType(deviceId, metricType, pageable);
    }

    public Page<TelemetryReading> getAnomalies(Pageable pageable) {
        return telemetryRepository.findByAnomalyTrue(pageable);
    }

    public Page<TelemetryReading> getAnomaliesByAccessibleDevices(List<UUID> deviceIds, Pageable pageable) {
        return telemetryRepository.findByAnomalyTrueAndDeviceIdIn(deviceIds, pageable);
    }

    public Page<TelemetryReading> getReadingsByAccessibleDevices(List<UUID> deviceIds, Pageable pageable) {
        return telemetryRepository.findByDeviceIdIn(deviceIds, pageable);
    }

    @Transactional
    public void deleteReading(UUID id) {
        TelemetryReading reading = getReadingById(id);
        telemetryRepository.delete(reading);
        log.info("Telemetry reading deleted: {}", id);
    }

    private AlertSeverity calculateSeverity(Double value, ThresholdRule rule) {
        double deviation = 0;
        if (rule.getMaxValue() != null && value > rule.getMaxValue()) {
            deviation = ((value - rule.getMaxValue()) / rule.getMaxValue()) * 100;
        } else if (rule.getMinValue() != null && value < rule.getMinValue()) {
            deviation = ((rule.getMinValue() - value) / rule.getMinValue()) * 100;
        }

        if (deviation > 50) return AlertSeverity.CRITICAL;
        if (deviation > 25) return AlertSeverity.HIGH;
        if (deviation > 10) return AlertSeverity.MEDIUM;
        return AlertSeverity.LOW;
    }

    private String buildAlertMessage(CreateTelemetryRequest request, ThresholdRule rule) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMetricType()).append(" anomaly detected: value=").append(request.getValue());
        if (rule.getMinValue() != null && request.getValue() < rule.getMinValue()) {
            sb.append(" is below minimum threshold ").append(rule.getMinValue());
        } else if (rule.getMaxValue() != null && request.getValue() > rule.getMaxValue()) {
            sb.append(" exceeds maximum threshold ").append(rule.getMaxValue());
        }
        return sb.toString();
    }

    private String getDefaultUnit(MetricType metricType) {
        return switch (metricType) {
            case TEMPERATURE -> "°C";
            case CPU_USAGE, MEMORY_USAGE, DISK_USAGE -> "%";
            case NETWORK_TRAFFIC -> "Mbps";
            case VOLTAGE -> "V";
            case HUMIDITY -> "%";
        };
    }
}