package com.iotplatform.dto.response;

import com.iotplatform.model.Alert;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.model.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private UUID id;
    private UUID deviceId;
    private UUID telemetryReadingId;
    private MetricType metricType;
    private AlertSeverity severity;
    private String message;
    private Double actualValue;
    private Double thresholdMin;
    private Double thresholdMax;
    private boolean resolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertResponse fromModel(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .deviceId(alert.getDeviceId())
                .telemetryReadingId(alert.getTelemetryReadingId())
                .metricType(alert.getMetricType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .actualValue(alert.getActualValue())
                .thresholdMin(alert.getThresholdMin())
                .thresholdMax(alert.getThresholdMax())
                .resolved(alert.isResolved())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}