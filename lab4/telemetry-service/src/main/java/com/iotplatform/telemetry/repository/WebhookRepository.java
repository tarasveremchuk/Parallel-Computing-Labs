package com.iotplatform.telemetry.repository;

import com.iotplatform.telemetry.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    List<Webhook> findByCreatedBy(UUID userId);

    @Query("SELECT w FROM Webhook w WHERE w.active = true AND (w.deviceId IS NULL OR w.deviceId = :deviceId)")
    List<Webhook> findApplicableWebhooks(@Param("deviceId") UUID deviceId);
}