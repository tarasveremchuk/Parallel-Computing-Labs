package com.iotplatform.model;

import com.iotplatform.model.enums.AlertSeverity;
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
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_device_id", columnList = "deviceId"),
        @Index(name = "idx_alert_severity", columnList = "severity"),
        @Index(name = "idx_alert_resolved", columnList = "resolved")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    private UUID telemetryReadingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(length = 500)
    private String message;

    private Double actualValue;
    private Double thresholdMin;
    private Double thresholdMax;

    @Builder.Default
    @Column(nullable = false)
    private boolean resolved = false;

    @Column(length = 1000)
    private String resolutionNote;

    private UUID resolvedBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}