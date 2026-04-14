package com.iotplatform.telemetry.model;

import com.iotplatform.telemetry.model.enums.MetricType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "telemetry_readings", indexes = {
        @Index(name = "idx_tel_device", columnList = "deviceId"),
        @Index(name = "idx_tel_timestamp", columnList = "timestamp"),
        @Index(name = "idx_tel_anomaly", columnList = "anomaly")
})
public class TelemetryReading {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID deviceId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private MetricType metricType;
    @Column(nullable = false)
    private double value;
    @Column(length = 20)
    private String unit;
    @Builder.Default @Column(nullable = false)
    private boolean anomaly = false;
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() { if (timestamp == null) timestamp = LocalDateTime.now(); }
}