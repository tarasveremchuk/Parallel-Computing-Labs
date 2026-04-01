package com.iotplatform.model;

import com.iotplatform.model.enums.MetricType;
import jakarta.persistence.*;
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
@Entity
@Table(name = "telemetry_readings", indexes = {
        @Index(name = "idx_telemetry_device_id", columnList = "deviceId"),
        @Index(name = "idx_telemetry_timestamp", columnList = "timestamp"),
        @Index(name = "idx_telemetry_anomaly", columnList = "anomaly")
})
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetricType metricType;

    @Column(nullable = false)
    private Double value;

    @Column(length = 20)
    private String unit;

    @Builder.Default
    @Column(nullable = false)
    private boolean anomaly = false;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}