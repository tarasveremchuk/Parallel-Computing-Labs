package com.iotplatform.device.repository;

import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Page<Device> findByDeletedFalse(Pageable pageable);
    Page<Device> findByDeletedFalseAndStatus(DeviceStatus status, Pageable pageable);
    Page<Device> findByDeletedFalseAndType(DeviceType type, Pageable pageable);
    Page<Device> findByDeletedFalseAndStatusAndType(DeviceStatus status, DeviceType type, Pageable pageable);
    Optional<Device> findByIdAndDeletedFalse(UUID id);
    Page<Device> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);
    List<Device> findByOwnerIdAndDeletedFalse(UUID ownerId);
    boolean existsByIdAndDeletedFalse(UUID id);
    long countByDeletedFalse();
    long countByDeletedFalseAndStatus(DeviceStatus status);
}