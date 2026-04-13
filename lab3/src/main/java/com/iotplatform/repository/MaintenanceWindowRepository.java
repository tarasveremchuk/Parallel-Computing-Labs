package com.iotplatform.repository;

import com.iotplatform.model.MaintenanceWindow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, UUID> {

    Page<MaintenanceWindow> findByDeviceIdAndCancelledFalse(UUID deviceId, Pageable pageable);

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.deviceId = :deviceId AND m.cancelled = false " +
            "AND m.startTime <= :now AND m.endTime >= :now")
    List<MaintenanceWindow> findActiveByDeviceId(@Param("deviceId") UUID deviceId, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.cancelled = false " +
            "AND m.startTime <= :now AND m.endTime >= :now")
    List<MaintenanceWindow> findAllActive(@Param("now") LocalDateTime now);

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.deviceId = :deviceId AND m.cancelled = false " +
            "AND m.startTime > :now ORDER BY m.startTime ASC")
    List<MaintenanceWindow> findUpcomingByDeviceId(@Param("deviceId") UUID deviceId, @Param("now") LocalDateTime now);

    Page<MaintenanceWindow> findByCancelledFalse(Pageable pageable);
}