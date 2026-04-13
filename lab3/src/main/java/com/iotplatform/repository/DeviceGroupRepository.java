package com.iotplatform.repository;

import com.iotplatform.model.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, UUID> {

    List<DeviceGroup> findByCreatedBy(UUID userId);

    boolean existsByNameIgnoreCase(String name);
}