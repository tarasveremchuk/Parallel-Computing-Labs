package com.iotplatform.repository;

import com.iotplatform.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<UUID, User> storage = new ConcurrentHashMap<>();

    public User save(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return storage.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return storage.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public boolean existsByEmail(String email) {
        return storage.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    public long count() {
        return storage.size();
    }
}