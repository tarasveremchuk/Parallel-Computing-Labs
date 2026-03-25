package com.iotplatform.model;

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
public class TelemetryReading {

    private UUID id;
    private UUID deviceId;
    private MetricType metricType;
    private Double value;
    private String unit;
    private boolean anomaly;
    private LocalDateTime timestamp;
}