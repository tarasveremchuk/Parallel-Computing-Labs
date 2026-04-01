package com.iotplatform.dto.request;

import com.iotplatform.model.enums.MetricType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateThresholdRuleRequest {

    private UUID deviceId;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    private Double minValue;

    private Double maxValue;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}