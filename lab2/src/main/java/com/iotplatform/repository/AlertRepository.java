package com.iotplatform.repository;

import com.iotplatform.model.Alert;
import com.iotplatform.model.enums.AlertSeverity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AlertRepository {

    private final Map<UUID, Alert> storage = new ConcurrentHashMap<>();

    public Alert save(Alert alert) {
        storage.put(alert.getId(), alert);
        return alert;
    }

    public Optional<Alert> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Alert> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> findByDeviceId(UUID deviceId) {
        return storage.values().stream()
                .filter(a -> a.getDeviceId().equals(deviceId))
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> findBySeverity(AlertSeverity severity) {
        return storage.values().stream()
                .filter(a -> a.getSeverity() == severity)
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> findUnresolved() {
        return storage.values().stream()
                .filter(a -> !a.isResolved())
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public long countUnresolved() {
        return storage.values().stream()
                .filter(a -> !a.isResolved())
                .count();
    }

    public long count() {
        return storage.size();
    }
}