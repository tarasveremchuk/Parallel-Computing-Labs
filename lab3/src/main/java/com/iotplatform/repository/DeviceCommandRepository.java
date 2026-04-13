package com.iotplatform.repository;

import com.iotplatform.model.DeviceCommand;
import com.iotplatform.model.enums.CommandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, UUID> {

    Page<DeviceCommand> findByDeviceId(UUID deviceId, Pageable pageable);

    Page<DeviceCommand> findByDeviceIdAndStatus(UUID deviceId, CommandStatus status, Pageable pageable);

    List<DeviceCommand> findByStatus(CommandStatus status);

    long countByDeviceIdAndStatus(UUID deviceId, CommandStatus status);
}