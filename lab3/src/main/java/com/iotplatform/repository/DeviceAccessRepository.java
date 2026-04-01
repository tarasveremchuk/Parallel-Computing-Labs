package com.iotplatform.repository;

import com.iotplatform.model.DeviceAccess;
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

    boolean existsByDeviceIdAndUserId(UUID deviceId, UUID userId);

    void deleteByDeviceIdAndUserId(UUID deviceId, UUID userId);

    @Query("SELECT da.deviceId FROM DeviceAccess da WHERE da.userId = :userId")
    List<UUID> findDeviceIdsByUserId(@Param("userId") UUID userId);
}