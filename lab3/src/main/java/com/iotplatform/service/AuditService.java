package com.iotplatform.service;

import com.iotplatform.model.AuditLog;
import com.iotplatform.model.enums.AuditAction;
import com.iotplatform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepository;

    @Transactional
    public void log(UUID userId, String username, AuditAction action,
                    String entityType, UUID entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditRepository.save(entry);
        log.debug("Audit: {} by {} on {}:{} — {}", action, username, entityType, entityId, details);
    }

    public Page<AuditLog> getAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    public Page<AuditLog> getByUserId(UUID userId, Pageable pageable) {
        return auditRepository.findByUserId(userId, pageable);
    }

    public Page<AuditLog> getByAction(AuditAction action, Pageable pageable) {
        return auditRepository.findByAction(action, pageable);
    }

    public Page<AuditLog> getByEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    public Page<AuditLog> getByTimeRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditRepository.findByTimestampBetween(from, to, pageable);
    }
}