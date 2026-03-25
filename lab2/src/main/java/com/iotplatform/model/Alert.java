package com.iotplatform.model;

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
public class Alert {

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
}