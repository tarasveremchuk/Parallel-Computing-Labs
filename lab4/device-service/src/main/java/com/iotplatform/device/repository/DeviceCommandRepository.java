package com.iotplatform.device.repository;

import com.iotplatform.device.model.DeviceCommand;
import com.iotplatform.device.model.enums.CommandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, UUID> {
    Page<DeviceCommand> findByDeviceId(UUID deviceId, Pageable pageable);
    long countByDeviceIdAndStatus(UUID deviceId, CommandStatus status);
}