package com.iotplatform.telemetry.model;

import com.iotplatform.telemetry.model.enums.AlertSeverity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "webhooks")
public class Webhook {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 500)
    private String url;
    @Enumerated(EnumType.STRING) @Column(length = 20)
    private AlertSeverity minSeverity;
    private UUID deviceId;
    @Builder.Default @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false)
    private UUID createdBy;
    private LocalDateTime lastTriggeredAt;
    @Builder.Default
    private long triggerCount = 0;
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}