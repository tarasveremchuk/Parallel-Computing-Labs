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
public class AlertStatsResponse {

    private long totalAlerts;
    private long unresolvedAlerts;
    private long resolvedAlerts;
    private double resolutionRate;
    private Map<String, Long> bySeverity;
    private Map<String, Long> unresolvedByDevice;
}