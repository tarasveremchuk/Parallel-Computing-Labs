package com.iotplatform.dto.request;

import com.iotplatform.model.enums.MetricType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTelemetryRequest {

    @NotNull(message = "Device ID is required")
    private UUID deviceId;

    @NotEmpty(message = "Readings list cannot be empty")
    @Valid
    private List<Reading> readings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reading {

        @NotNull(message = "Metric type is required")
        private MetricType metricType;

        @NotNull(message = "Value is required")
        private Double value;

        private String unit;
    }
}