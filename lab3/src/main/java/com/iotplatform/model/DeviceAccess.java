package com.iotplatform.model;

import com.iotplatform.model.enums.Permission;
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
@Table(name = "device_access", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"deviceId", "userId"})
})
public class DeviceAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Permission permission;

    @Column(nullable = false)
    private UUID grantedBy;

    @Column(updatable = false)
    private LocalDateTime grantedAt;

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}