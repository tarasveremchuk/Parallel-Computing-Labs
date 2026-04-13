package com.iotplatform.model;

import com.iotplatform.model.enums.CommandStatus;
import com.iotplatform.model.enums.CommandType;
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
@Table(name = "device_commands", indexes = {
        @Index(name = "idx_command_device", columnList = "deviceId"),
        @Index(name = "idx_command_status", columnList = "status")
})
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
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
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(30);
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt)
                && status == CommandStatus.PENDING;
    }
}