package com.iotplatform.service;

import com.iotplatform.model.Alert;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.repository.AlertRepository;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;

    public String exportTelemetryCsv(List<UUID> deviceIds) {
        List<TelemetryReading> readings = telemetryRepository.findByDeviceIdIn(
                deviceIds, PageRequest.of(0, 10000, Sort.by("timestamp").descending())
        ).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("id,deviceId,metricType,value,unit,anomaly,timestamp\n");

        for (TelemetryReading r : readings) {
            csv.append(r.getId()).append(",")
                    .append(r.getDeviceId()).append(",")
                    .append(r.getMetricType()).append(",")
                    .append(r.getValue()).append(",")
                    .append(r.getUnit() != null ? r.getUnit() : "").append(",")
                    .append(r.isAnomaly()).append(",")
                    .append(r.getTimestamp()).append("\n");
        }

        log.info("Exported {} telemetry readings to CSV", readings.size());
        return csv.toString();
    }

    public String exportAlertsCsv(List<UUID> deviceIds) {
        List<Alert> alerts = alertRepository.findByDeviceIdIn(
                deviceIds, PageRequest.of(0, 10000, Sort.by("createdAt").descending())
        ).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("id,deviceId,metricType,severity,message,actualValue,thresholdMin,thresholdMax,resolved,resolutionNote,createdAt,resolvedAt\n");

        for (Alert a : alerts) {
            csv.append(a.getId()).append(",")
                    .append(a.getDeviceId()).append(",")
                    .append(a.getMetricType()).append(",")
                    .append(a.getSeverity()).append(",")
                    .append("\"").append(a.getMessage() != null ? a.getMessage().replace("\"", "\"\"") : "").append("\"").append(",")
                    .append(a.getActualValue()).append(",")
                    .append(a.getThresholdMin() != null ? a.getThresholdMin() : "").append(",")
                    .append(a.getThresholdMax() != null ? a.getThresholdMax() : "").append(",")
                    .append(a.isResolved()).append(",")
                    .append("\"").append(a.getResolutionNote() != null ? a.getResolutionNote().replace("\"", "\"\"") : "").append("\"").append(",")
                    .append(a.getCreatedAt()).append(",")
                    .append(a.getResolvedAt() != null ? a.getResolvedAt() : "").append("\n");
        }

        log.info("Exported {} alerts to CSV", alerts.size());
        return csv.toString();
    }
}