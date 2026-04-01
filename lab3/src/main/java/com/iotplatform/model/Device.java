package com.iotplatform.model;

import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
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
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(length = 50)
    private String firmwareVersion;

    @Column(nullable = false)
    private UUID ownerId;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @Column(updatable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime updatedAt;
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastSeenAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}