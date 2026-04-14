package com.iotplatform.device.repository;

import com.iotplatform.device.model.ThresholdRule;
import com.iotplatform.device.model.enums.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, UUID> {
    Page<ThresholdRule> findByDeletedFalse(Pageable pageable);

    @Query("SELECT r FROM ThresholdRule r WHERE r.deleted = false AND r.active = true " +
            "AND r.metricType = :metricType AND (r.deviceId = :deviceId OR r.deviceId IS NULL) " +
            "ORDER BY CASE WHEN r.deviceId IS NOT NULL THEN 0 ELSE 1 END")
    List<ThresholdRule> findApplicableRules(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    Page<ThresholdRule> findByDeviceIdAndDeletedFalse(UUID deviceId, Pageable pageable);
}