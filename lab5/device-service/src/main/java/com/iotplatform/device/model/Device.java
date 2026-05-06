package com.iotplatform.device.model;

import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "devices")
public class Device implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private DeviceType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private DeviceStatus status;
    @Column(length = 200)
    private String location;
    @Column(length = 50)
    private String firmwareVersion;
    @Column(nullable = false)
    private UUID ownerId;
    @Builder.Default @Column(nullable = false)
    private boolean deleted = false;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}