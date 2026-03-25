package com.iotplatform.repository;

import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.MetricType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ThresholdRuleRepository {

    private final Map<UUID, ThresholdRule> storage = new ConcurrentHashMap<>();

    public ThresholdRule save(ThresholdRule rule) {
        storage.put(rule.getId(), rule);
        return rule;
    }

    public Optional<ThresholdRule> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<ThresholdRule> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<ThresholdRule> findByDeviceId(UUID deviceId) {
        return storage.values().stream()
                .filter(r -> deviceId.equals(r.getDeviceId()))
                .collect(Collectors.toList());
    }

    public List<ThresholdRule> findApplicableRules(UUID deviceId, MetricType metricType) {
        return storage.values().stream()
                .filter(ThresholdRule::isActive)
                .filter(r -> r.getMetricType() == metricType)
                .filter(r -> r.getDeviceId() == null || r.getDeviceId().equals(deviceId))
                .sorted(Comparator.comparing(r -> r.getDeviceId() == null ? 1 : 0))
                .collect(Collectors.toList());
    }

    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    public void deleteById(UUID id) {
        storage.remove(id);
    }

    public long count() {
        return storage.size();
    }
}