package com.iotplatform.dto.response;

import com.iotplatform.model.TelemetryReading;
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
public class TelemetryResponse {

    private UUID id;
    private UUID deviceId;
    private MetricType metricType;
    private Double value;
    private String unit;
    private boolean anomaly;
    private LocalDateTime timestamp;

    public static TelemetryResponse fromModel(TelemetryReading reading) {
        return TelemetryResponse.builder()
                .id(reading.getId())
                .deviceId(reading.getDeviceId())
                .metricType(reading.getMetricType())
                .value(reading.getValue())
                .unit(reading.getUnit())
                .anomaly(reading.isAnomaly())
                .timestamp(reading.getTimestamp())
                .build();
    }
}