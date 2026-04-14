package com.iotplatform.telemetry.service;

import com.iotplatform.telemetry.model.AuditLog;
import com.iotplatform.telemetry.model.enums.AuditAction;
import com.iotplatform.telemetry.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepository;

    public void log(UUID userId, String username, AuditAction action, String entityType, UUID entityId, String details) {
        auditRepository.save(AuditLog.builder().userId(userId).username(username)
                .action(action).entityType(entityType).entityId(entityId).details(details).build());
    }

    public Page<AuditLog> getAll(Pageable pageable) { return auditRepository.findAll(pageable); }
}