package com.iotplatform.service;

import com.iotplatform.dto.request.CreateThresholdRuleRequest;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.ThresholdRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThresholdRuleService {

    private final ThresholdRuleRepository ruleRepository;

    public ThresholdRule createRule(CreateThresholdRuleRequest request) {
        if (request.getMinValue() == null && request.getMaxValue() == null) {
            throw new InvalidOperationException(
                    "At least one threshold boundary (minValue or maxValue) must be specified");
        }

        if (request.getMinValue() != null && request.getMaxValue() != null
                && request.getMinValue() >= request.getMaxValue()) {
            throw new InvalidOperationException("minValue must be less than maxValue");
        }

        ThresholdRule rule = ThresholdRule.builder()
                .id(UUID.randomUUID())
                .deviceId(request.getDeviceId())
                .metricType(request.getMetricType())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .description(request.getDescription())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        ThresholdRule saved = ruleRepository.save(rule);
        log.info("Threshold rule created: {} for metric {} [min={}, max={}]",
                saved.getId(), saved.getMetricType(), saved.getMinValue(), saved.getMaxValue());
        return saved;
    }

    public List<ThresholdRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public ThresholdRule getRuleById(UUID id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdRule", "id", id));
    }

    public List<ThresholdRule> getApplicableRules(UUID deviceId, MetricType metricType) {
        return ruleRepository.findApplicableRules(deviceId, metricType);
    }

    public List<ThresholdRule> getRulesByDeviceId(UUID deviceId) {
        return ruleRepository.findByDeviceId(deviceId);
    }
}