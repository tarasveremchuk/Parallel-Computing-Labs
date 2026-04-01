package com.iotplatform.dto.request;

import com.iotplatform.model.enums.MetricType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTelemetryRequest {

    @NotNull(message = "Device ID is required")
    private UUID deviceId;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    @NotNull(message = "Value is required")
    private Double value;

    private String unit;
}