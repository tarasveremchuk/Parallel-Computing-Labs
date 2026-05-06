package com.iotplatform.telemetry.model;

import com.iotplatform.telemetry.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String username;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private AuditAction action;
    @Column(length = 50)
    private String entityType;
    private UUID entityId;
    @Column(length = 500)
    private String details;
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() { timestamp = LocalDateTime.now(); }
}