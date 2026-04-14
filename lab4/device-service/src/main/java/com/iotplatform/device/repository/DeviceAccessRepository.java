package com.iotplatform.device.repository;

import com.iotplatform.device.model.DeviceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceAccessRepository extends JpaRepository<DeviceAccess, UUID> {
    List<DeviceAccess> findByDeviceId(UUID deviceId);
    List<DeviceAccess> findByUserId(UUID userId);
    Optional<DeviceAccess> findByDeviceIdAndUserId(UUID deviceId, UUID userId);
    void deleteByDeviceIdAndUserId(UUID deviceId, UUID userId);

    @Query("SELECT a.deviceId FROM DeviceAccess a WHERE a.userId = :userId")
    List<UUID> findDeviceIdsByUserId(@Param("userId") UUID userId);
}