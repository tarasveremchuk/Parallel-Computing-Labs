package com.iotplatform.model;

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
@Table(name = "maintenance_windows", indexes = {
        @Index(name = "idx_maintenance_device", columnList = "deviceId"),
        @Index(name = "idx_maintenance_times", columnList = "startTime,endTime")
})
public class MaintenanceWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private UUID scheduledBy;

    @Builder.Default
    @Column(nullable = false)
    private boolean cancelled = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        if (cancelled) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    public boolean isUpcoming() {
        if (cancelled) return false;
        return LocalDateTime.now().isBefore(startTime);
    }
}