package com.iotplatform.repository;

import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.enums.MetricType;
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

    Page<TelemetryReading> findByDeviceIdIn(List<UUID> deviceIds, Pageable pageable);

    Page<TelemetryReading> findByAnomalyTrueAndDeviceIdIn(List<UUID> deviceIds, Pageable pageable);

    long countByDeviceId(UUID deviceId);

    long countByDeviceIdAndAnomalyTrue(UUID deviceId);

    @Query("SELECT AVG(t.value) FROM TelemetryReading t WHERE t.deviceId = :deviceId AND t.metricType = :metricType")
    Double findAvgValueByDeviceIdAndMetricType(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    @Query("SELECT MIN(t.value) FROM TelemetryReading t WHERE t.deviceId = :deviceId AND t.metricType = :metricType")
    Double findMinValueByDeviceIdAndMetricType(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    @Query("SELECT MAX(t.value) FROM TelemetryReading t WHERE t.deviceId = :deviceId AND t.metricType = :metricType")
    Double findMaxValueByDeviceIdAndMetricType(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    @Query("SELECT COUNT(t) FROM TelemetryReading t WHERE t.deviceId = :deviceId AND t.metricType = :metricType")
    long countByDeviceIdAndMetricType(@Param("deviceId") UUID deviceId, @Param("metricType") MetricType metricType);

    @Query("SELECT DISTINCT t.metricType FROM TelemetryReading t WHERE t.deviceId = :deviceId")
    List<MetricType> findDistinctMetricTypesByDeviceId(@Param("deviceId") UUID deviceId);

    List<TelemetryReading> findTop1ByDeviceIdAndMetricTypeOrderByTimestampDesc(UUID deviceId, MetricType metricType);
}