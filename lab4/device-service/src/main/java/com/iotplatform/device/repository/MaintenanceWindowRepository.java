package com.iotplatform.device.repository;

import com.iotplatform.device.model.MaintenanceWindow;
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
    @Query("SELECT m FROM MaintenanceWindow m WHERE m.deviceId = :deviceId AND m.cancelled = false " +
            "AND m.startTime <= :now AND m.endTime >= :now")
    List<MaintenanceWindow> findActiveByDeviceId(@Param("deviceId") UUID deviceId, @Param("now") LocalDateTime now);

    Page<MaintenanceWindow> findByDeviceIdAndCancelledFalse(UUID deviceId, Pageable pageable);
    Page<MaintenanceWindow> findByCancelledFalse(Pageable pageable);
}