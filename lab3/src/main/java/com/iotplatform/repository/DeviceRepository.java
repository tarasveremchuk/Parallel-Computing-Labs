package com.iotplatform.repository;

import com.iotplatform.model.Device;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findByIdAndDeletedFalse(UUID id);

    Page<Device> findByDeletedFalse(Pageable pageable);

    Page<Device> findByStatusAndDeletedFalse(DeviceStatus status, Pageable pageable);

    Page<Device> findByTypeAndDeletedFalse(DeviceType type, Pageable pageable);

    Page<Device> findByLocationContainingIgnoreCaseAndDeletedFalse(String location, Pageable pageable);

    Page<Device> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByIdAndDeletedFalse(UUID id);

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(DeviceStatus status);

    @Query("SELECT d FROM Device d WHERE d.deleted = false AND (d.ownerId = :userId OR d.id IN " +
            "(SELECT da.deviceId FROM DeviceAccess da WHERE da.userId = :userId))")
    Page<Device> findAccessibleDevices(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT d FROM Device d WHERE d.deleted = false AND (d.ownerId = :userId OR d.id IN " +
            "(SELECT da.deviceId FROM DeviceAccess da WHERE da.userId = :userId))")
    List<Device> findAllAccessibleDevices(@Param("userId") UUID userId);
}