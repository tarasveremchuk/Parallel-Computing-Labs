package com.iotplatform.repository;

import com.iotplatform.model.AuditLog;
import com.iotplatform.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByUserIdAndTimestampBetween(UUID userId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}