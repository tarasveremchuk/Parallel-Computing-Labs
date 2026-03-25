package com.iotplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
    private long errorDevices;

    private long totalReadings;
    private long totalAnomalies;
    private double anomalyRate;

    private long totalAlerts;
    private long unresolvedAlerts;
    private Map<String, Long> alertsBySeverity;

    private Map<String, DeviceHealth> deviceHealthMap;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceHealth {
        private String deviceName;
        private long totalReadings;
        private long anomalyCount;
        private double healthScore;
        private Double lastTemperature;
        private Double lastCpuUsage;
    }
}