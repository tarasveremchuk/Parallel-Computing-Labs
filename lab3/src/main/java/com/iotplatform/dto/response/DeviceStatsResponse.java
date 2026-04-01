package com.iotplatform.dto.response;

import com.iotplatform.model.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatsResponse {

    private UUID deviceId;
    private String deviceName;
    private long totalReadings;
    private long anomalyCount;
    private double healthScore;
    private long unresolvedAlerts;
    private Map<MetricType, MetricStats> metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricStats {
        private Double avg;
        private Double min;
        private Double max;
        private Double lastValue;
        private long readingCount;
    }
}