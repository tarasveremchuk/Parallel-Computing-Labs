package com.iotplatform.repository;

import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.MetricType;
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
public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, UUID> {

    Optional<ThresholdRule> findByIdAndDeletedFalse(UUID id);

    Page<ThresholdRule> findByDeletedFalse(Pageable pageable);

    List<ThresholdRule> findByDeviceIdAndDeletedFalse(UUID deviceId);

    @Query("SELECT r FROM ThresholdRule r WHERE r.active = true AND r.deleted = false " +
            "AND r.metricType = :metricType AND (r.deviceId IS NULL OR r.deviceId = :deviceId) " +
            "ORDER BY CASE WHEN r.deviceId IS NULL THEN 1 ELSE 0 END")
    List<ThresholdRule> findApplicableRules(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    boolean existsByIdAndDeletedFalse(UUID id);
}