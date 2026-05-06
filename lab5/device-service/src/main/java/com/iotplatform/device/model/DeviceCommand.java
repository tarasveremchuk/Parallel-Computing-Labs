package com.iotplatform.device.model;

import com.iotplatform.device.model.enums.CommandStatus;
import com.iotplatform.device.model.enums.CommandType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "device_commands")
public class DeviceCommand {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID deviceId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private CommandType commandType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CommandStatus status;
    @Column(length = 500)
    private String payload;
    @Column(length = 500)
    private String response;
    @Column(nullable = false)
    private UUID sentBy;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); if (expiresAt == null) expiresAt = LocalDateTime.now().plusMinutes(30); }
}