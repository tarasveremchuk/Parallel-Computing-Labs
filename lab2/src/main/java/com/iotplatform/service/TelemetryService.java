package com.iotplatform.service;

import com.iotplatform.dto.request.CreateTelemetryRequest;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public TelemetryReading ingestTelemetry(CreateTelemetryRequest request) {
        // 1. Validate device exists
        if (!deviceService.existsById(request.getDeviceId())) {
            throw new ResourceNotFoundException("Device", "id", request.getDeviceId());
        }

        // 2. Build telemetry reading
        TelemetryReading reading = TelemetryReading.builder()
                .id(UUID.randomUUID())
                .deviceId(request.getDeviceId())
                .metricType(request.getMetricType())
                .value(request.getValue())
                .unit(request.getUnit() != null ? request.getUnit() : getDefaultUnit(request.getMetricType()))
                .anomaly(false)
                .timestamp(LocalDateTime.now())
                .build();

        // 3. RULE ENGINE: evaluate thresholds
        List<ThresholdRule> rules = thresholdRuleService.getApplicableRules(
                request.getDeviceId(), request.getMetricType());

        for (ThresholdRule rule : rules) {
            if (rule.isViolated(request.getValue())) {
                reading.setAnomaly(true);

                AlertSeverity severity = calculateSeverity(request.getValue(), rule);
                String message = buildAlertMessage(request, rule);

                Alert alert = Alert.builder()
                        .id(UUID.randomUUID())
                        .deviceId(request.getDeviceId())
                        .telemetryReadingId(reading.getId())
                        .metricType(request.getMetricType())
                        .severity(severity)
                        .message(message)
                        .actualValue(request.getValue())
                        .thresholdMin(rule.getMinValue())
                        .thresholdMax(rule.getMaxValue())
                        .resolved(false)
                        .createdAt(LocalDateTime.now())
                        .build();

                alertService.createAlert(alert);
                log.warn("ANOMALY DETECTED on device {}: {} = {} (rule: {})",
                        request.getDeviceId(), request.getMetricType(),
                        request.getValue(), rule.getId());
                break;
            }
        }

        // 4. Save and return
        TelemetryReading saved = telemetryRepository.save(reading);
        log.debug("Telemetry ingested: device={}, metric={}, value={}{}",
                saved.getDeviceId(), saved.getMetricType(), saved.getValue(),
                saved.isAnomaly() ? " [ANOMALY]" : "");
        return saved;
    }

    public List<TelemetryReading> getAllReadings() {
        return telemetryRepository.findAll();
    }

    public TelemetryReading getReadingById(UUID id) {
        return telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TelemetryReading", "id", id));
    }

    public List<TelemetryReading> getReadingsByDeviceId(UUID deviceId) {
        if (!deviceService.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        return telemetryRepository.findByDeviceId(deviceId);
    }

    public List<TelemetryReading> getReadingsByDeviceAndMetric(UUID deviceId, MetricType metricType) {
        if (!deviceService.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        return telemetryRepository.findByDeviceIdAndMetricType(deviceId, metricType);
    }

    public List<TelemetryReading> getAnomalies() {
        return telemetryRepository.findAnomalies();
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