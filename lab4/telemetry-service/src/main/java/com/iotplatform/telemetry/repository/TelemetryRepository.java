package com.iotplatform.telemetry.repository;

import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.enums.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryReading, UUID> {
    Page<TelemetryReading> findByDeviceId(UUID deviceId, Pageable pageable);
    Page<TelemetryReading> findByDeviceIdAndMetricType(UUID deviceId, MetricType metricType, Pageable pageable);
    Page<TelemetryReading> findByAnomalyTrue(Pageable pageable);
    long countByDeviceId(UUID deviceId);
    long countByDeviceIdAndAnomalyTrue(UUID deviceId);
    long countByAnomalyTrue();

    @Query("SELECT t FROM TelemetryReading t WHERE t.deviceId IN :deviceIds")
    Page<TelemetryReading> findByDeviceIdIn(@Param("deviceIds") List<UUID> deviceIds, Pageable pageable);
}