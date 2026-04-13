package com.iotplatform.service;

import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Alert;
import com.iotplatform.model.Webhook;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;

    @Transactional
    public Webhook create(String name, String url, AlertSeverity minSeverity,
                          UUID deviceId, UUID createdBy) {
        if (webhookRepository.existsByUrlAndCreatedBy(url, createdBy)) {
            throw new DuplicateResourceException("You already have a webhook with this URL");
        }

        Webhook webhook = Webhook.builder()
                .name(name)
                .url(url)
                .minSeverity(minSeverity)
                .deviceId(deviceId)
                .active(true)
                .createdBy(createdBy)
                .build();

        Webhook saved = webhookRepository.save(webhook);
        log.info("Webhook created: {} -> {} by user {}", name, url, createdBy);
        return saved;
    }

    public List<Webhook> getByUser(UUID userId) {
        return webhookRepository.findByCreatedBy(userId);
    }

    public Webhook getById(UUID id) {
        return webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", "id", id));
    }

    @Transactional
    public Webhook toggle(UUID id) {
        Webhook webhook = getById(id);
        webhook.setActive(!webhook.isActive());
        webhookRepository.save(webhook);
        log.info("Webhook {} toggled to {}", id, webhook.isActive());
        return webhook;
    }

    @Transactional
    public void delete(UUID id) {
        Webhook webhook = getById(id);
        webhookRepository.delete(webhook);
        log.info("Webhook deleted: {}", id);
    }

    @Transactional
    public void triggerWebhooks(Alert alert) {
        List<Webhook> webhooks = webhookRepository.findApplicableWebhooks(alert.getDeviceId());

        for (Webhook webhook : webhooks) {
            if (!shouldTrigger(webhook, alert)) continue;

            try {
                // In production: use RestTemplate/WebClient to POST to webhook.url
                // For demo: just log and increment counter
                log.info("WEBHOOK TRIGGERED: {} -> {} | Alert: {} [{}] on device {}",
                        webhook.getName(), webhook.getUrl(),
                        alert.getMetricType(), alert.getSeverity(), alert.getDeviceId());

                webhook.setLastTriggeredAt(LocalDateTime.now());
                webhook.setTriggerCount(webhook.getTriggerCount() + 1);
                webhookRepository.save(webhook);
            } catch (Exception e) {
                log.error("Webhook {} failed to trigger: {}", webhook.getId(), e.getMessage());
            }
        }
    }

    private boolean shouldTrigger(Webhook webhook, Alert alert) {
        if (webhook.getMinSeverity() == null) return true;
        int alertLevel = severityLevel(alert.getSeverity());
        int minLevel = severityLevel(webhook.getMinSeverity());
        return alertLevel >= minLevel;
    }

    private int severityLevel(AlertSeverity severity) {
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}