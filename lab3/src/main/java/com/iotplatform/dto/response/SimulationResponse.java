package com.iotplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {

    private int totalReadingsGenerated;
    private int anomaliesDetected;
    private int alertsCreated;
    private long executionTimeMs;
    private String summary;
}