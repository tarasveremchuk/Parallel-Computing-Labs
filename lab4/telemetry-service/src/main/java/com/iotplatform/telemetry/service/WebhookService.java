package com.iotplatform.telemetry.service;

import com.iotplatform.telemetry.exception.ResourceNotFoundException;
import com.iotplatform.telemetry.model.Alert;
import com.iotplatform.telemetry.model.Webhook;
import com.iotplatform.telemetry.model.enums.AlertSeverity;
import com.iotplatform.telemetry.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;

    @Transactional
    public Webhook create(String name, String url, AlertSeverity minSeverity, UUID deviceId, UUID createdBy) {
        Webhook webhook = Webhook.builder().name(name).url(url).minSeverity(minSeverity)
                .deviceId(deviceId).active(true).createdBy(createdBy).build();
        return webhookRepository.save(webhook);
    }

    public List<Webhook> getByUser(UUID userId) { return webhookRepository.findByCreatedBy(userId); }

    public Webhook getById(UUID id) {
        return webhookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Webhook", "id", id));
    }

    @Transactional
    public Webhook toggle(UUID id) {
        Webhook w = getById(id);
        w.setActive(!w.isActive());
        return webhookRepository.save(w);
    }

    @Transactional
    public void delete(UUID id) { webhookRepository.delete(getById(id)); }

    @Transactional
    public void triggerWebhooks(Alert alert) {
        List<Webhook> webhooks = webhookRepository.findApplicableWebhooks(alert.getDeviceId());
        for (Webhook w : webhooks) {
            if (w.getMinSeverity() != null && severityLevel(alert.getSeverity()) < severityLevel(w.getMinSeverity())) continue;
            log.info("WEBHOOK: {} -> {} | Alert {} [{}]", w.getName(), w.getUrl(), alert.getMetricType(), alert.getSeverity());
            w.setLastTriggeredAt(LocalDateTime.now());
            w.setTriggerCount(w.getTriggerCount() + 1);
            webhookRepository.save(w);
        }
    }

    private int severityLevel(AlertSeverity s) {
        return switch (s) { case LOW -> 1; case MEDIUM -> 2; case HIGH -> 3; case CRITICAL -> 4; };
    }
}