package com.iotplatform.telemetry.service;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.exception.InvalidOperationException;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.model.enums.MetricType;
import com.iotplatform.telemetry.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final DeviceServiceClient deviceServiceClient;
    private final AlertService alertService;
    private final WebhookService webhookService;

    @Transactional
    public TelemetryReading ingest(UUID deviceId, MetricType metricType, double value, String unit) {
        // Verify device exists via Device Service
        try {
            Boolean exists = deviceServiceClient.deviceExists(deviceId);
            if (exists == null || !exists) {
                throw new InvalidOperationException("Device not found: " + deviceId);
            }
        } catch (feign.FeignException e) {
            log.error("Device Service unavailable: {}", e.getMessage());
            throw new InvalidOperationException("Device Service is unavailable. Please try again later.");
        }

        TelemetryReading reading = TelemetryReading.builder()
                .deviceId(deviceId).metricType(metricType).value(value).unit(unit).anomaly(false).build();
        reading = telemetryRepository.save(reading);

        // Check maintenance via Device Service
        boolean underMaintenance = false;
        try {
            Boolean maint = deviceServiceClient.isUnderMaintenance(deviceId);
            underMaintenance = maint != null && maint;
        } catch (feign.FeignException e) {
            log.warn("Could not check maintenance status: {}", e.getMessage());
        }

        if (!underMaintenance) {
            // Get applicable rules from Device Service
            try {
                List<Map<String, Object>> rules = deviceServiceClient.getApplicableRules(deviceId, metricType.name());
                for (Map<String, Object> rule : rules) {
                    Double minValue = rule.get("minValue") != null ? ((Number) rule.get("minValue")).doubleValue() : null;
                    Double maxValue = rule.get("maxValue") != null ? ((Number) rule.get("maxValue")).doubleValue() : null;
                    boolean violated = false;
                    if (minValue != null && value < minValue) violated = true;
                    if (maxValue != null && value > maxValue) violated = true;

                    if (violated) {
                        reading.setAnomaly(true);
                        telemetryRepository.save(reading);

                        AlertSeverity severity = calculateSeverity(value, minValue, maxValue);
                        String message = String.format("Anomaly detected: %s = %.2f (threshold: %.1f - %.1f)",
                                metricType, value, minValue != null ? minValue : 0, maxValue != null ? maxValue : 0);

                        Alert alert = Alert.builder()
                                .deviceId(deviceId).telemetryReadingId(reading.getId())
                                .metricType(metricType).severity(severity).message(message)
                                .actualValue(value).thresholdMin(minValue).thresholdMax(maxValue)
                                .resolved(false).build();
                        Alert savedAlert = alertService.createAlert(alert);
                        webhookService.triggerWebhooks(savedAlert);
                        break;
                    }
                }
            } catch (feign.FeignException e) {
                log.warn("Could not fetch rules from Device Service: {}", e.getMessage());
            }
        }

        return reading;
    }

    private AlertSeverity calculateSeverity(double value, Double min, Double max) {
        double deviation = 0;
        if (max != null && value > max) {
            deviation = max != 0 ? ((value - max) / Math.abs(max)) * 100 : 100;
        } else if (min != null && value < min) {
            deviation = min != 0 ? ((min - value) / Math.abs(min)) * 100 : 100;
        }
        if (deviation > 50) return AlertSeverity.CRITICAL;
        if (deviation > 25) return AlertSeverity.HIGH;
        if (deviation > 10) return AlertSeverity.MEDIUM;
        return AlertSeverity.LOW;
    }

    public Page<TelemetryReading> getAll(Pageable pageable) { return telemetryRepository.findAll(pageable); }
    public Page<TelemetryReading> getByDevice(UUID deviceId, MetricType metricType, Pageable pageable) {
        if (metricType != null) return telemetryRepository.findByDeviceIdAndMetricType(deviceId, metricType, pageable);
        return telemetryRepository.findByDeviceId(deviceId, pageable);
    }
    public long count() { return telemetryRepository.count(); }
    public long countAnomalies() { return telemetryRepository.countByAnomalyTrue(); }
    public long countByDevice(UUID deviceId) { return telemetryRepository.countByDeviceId(deviceId); }
    public long countAnomaliesByDevice(UUID deviceId) { return telemetryRepository.countByDeviceIdAndAnomalyTrue(deviceId); }
}