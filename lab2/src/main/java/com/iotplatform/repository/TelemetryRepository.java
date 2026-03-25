package com.iotplatform.repository;

import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.enums.MetricType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TelemetryRepository {

    private final Map<UUID, TelemetryReading> storage = new ConcurrentHashMap<>();

    public TelemetryReading save(TelemetryReading reading) {
        storage.put(reading.getId(), reading);
        return reading;
    }

    public Optional<TelemetryReading> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<TelemetryReading> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(TelemetryReading::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<TelemetryReading> findByDeviceId(UUID deviceId) {
        return storage.values().stream()
                .filter(r -> r.getDeviceId().equals(deviceId))
                .sorted(Comparator.comparing(TelemetryReading::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<TelemetryReading> findByDeviceIdAndMetricType(UUID deviceId, MetricType metricType) {
        return storage.values().stream()
                .filter(r -> r.getDeviceId().equals(deviceId) && r.getMetricType() == metricType)
                .sorted(Comparator.comparing(TelemetryReading::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<TelemetryReading> findAnomalies() {
        return storage.values().stream()
                .filter(TelemetryReading::isAnomaly)
                .sorted(Comparator.comparing(TelemetryReading::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public long countByDeviceId(UUID deviceId) {
        return storage.values().stream()
                .filter(r -> r.getDeviceId().equals(deviceId))
                .count();
    }

    public long count() {
        return storage.size();
    }
}