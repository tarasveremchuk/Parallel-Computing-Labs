package com.iotplatform.telemetry.repository;

import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    Page<Alert> findByDeviceId(UUID deviceId, Pageable pageable);
    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);
    Page<Alert> findByResolvedFalse(Pageable pageable);
    long countByResolvedFalse();
    long countBySeverity(AlertSeverity severity);
    long countByDeviceId(UUID deviceId);
    long countByDeviceIdAndResolvedFalse(UUID deviceId);

    @Query("SELECT a FROM Alert a WHERE a.deviceId IN :deviceIds")
    Page<Alert> findByDeviceIdIn(@Param("deviceIds") List<UUID> deviceIds, Pageable pageable);
}