package com.iotplatform.device.model;

import com.iotplatform.device.model.enums.MetricType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "threshold_rules")
public class ThresholdRule {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private MetricType metricType;
    private Double minValue;
    private Double maxValue;
    @Column(length = 255)
    private String description;
    private UUID deviceId;
    @Builder.Default @Column(nullable = false)
    private boolean active = true;
    @Builder.Default @Column(nullable = false)
    private boolean deleted = false;
    private UUID createdBy;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean isViolated(double value) {
        if (minValue != null && value < minValue) return true;
        if (maxValue != null && value > maxValue) return true;
        return false;
    }
}