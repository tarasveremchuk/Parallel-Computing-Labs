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
public class ThresholdRule {

    private UUID id;
    private UUID deviceId;
    private MetricType metricType;
    private Double minValue;
    private Double maxValue;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public boolean isViolated(Double value) {
        if (value == null) return false;
        if (minValue != null && value < minValue) return true;
        if (maxValue != null && value > maxValue) return true;
        return false;
    }
}