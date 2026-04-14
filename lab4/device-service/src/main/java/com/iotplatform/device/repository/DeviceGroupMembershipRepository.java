package com.iotplatform.device.repository;

import com.iotplatform.device.model.DeviceGroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceGroupMembershipRepository extends JpaRepository<DeviceGroupMembership, UUID> {
    List<DeviceGroupMembership> findByGroupId(UUID groupId);
    boolean existsByGroupIdAndDeviceId(UUID groupId, UUID deviceId);
    void deleteByGroupIdAndDeviceId(UUID groupId, UUID deviceId);
    void deleteByGroupId(UUID groupId);
    @Query("SELECT m.deviceId FROM DeviceGroupMembership m WHERE m.groupId = :groupId")
    List<UUID> findDeviceIdsByGroupId(@Param("groupId") UUID groupId);
    long countByGroupId(UUID groupId);
}