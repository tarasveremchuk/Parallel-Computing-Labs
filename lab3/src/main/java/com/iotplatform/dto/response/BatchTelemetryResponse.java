package com.iotplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTelemetryResponse {

    private int totalReceived;
    private int successCount;
    private int anomalyCount;
    private int alertsGenerated;
    private List<TelemetryResponse> readings;
}