package com.iotplatform.repository;

import com.iotplatform.model.Alert;
import com.iotplatform.model.enums.AlertSeverity;
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

    Page<Alert> findByDeviceIdIn(List<UUID> deviceIds, Pageable pageable);

    Page<Alert> findByResolvedFalseAndDeviceIdIn(List<UUID> deviceIds, Pageable pageable);

    Page<Alert> findBySeverityAndDeviceIdIn(AlertSeverity severity, List<UUID> deviceIds, Pageable pageable);

    long countByResolvedFalse();

    long countByDeviceId(UUID deviceId);

    long countByResolvedFalseAndDeviceIdIn(List<UUID> deviceIds);

    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.deviceId IN :deviceIds GROUP BY a.severity")
    List<Object[]> countBySeverityGrouped(@Param("deviceIds") List<UUID> deviceIds);

    @Query("SELECT a.deviceId, COUNT(a) FROM Alert a WHERE a.deviceId IN :deviceIds AND a.resolved = false GROUP BY a.deviceId")
    List<Object[]> countUnresolvedByDeviceGrouped(@Param("deviceIds") List<UUID> deviceIds);
}