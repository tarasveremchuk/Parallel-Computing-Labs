package com.iotplatform.device.repository;

import com.iotplatform.device.model.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, UUID> {
    boolean existsByNameIgnoreCase(String name);
}