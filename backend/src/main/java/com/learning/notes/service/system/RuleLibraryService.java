package com.learning.notes.service.system;

import com.learning.notes.entity.system.RuleLibrary;
import com.learning.notes.repository.system.RuleLibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleLibraryService {

    private final RuleLibraryRepository ruleLibraryRepository;

    public List<RuleLibrary> getUserRules(Long userId) {
        return ruleLibraryRepository.findByUserId(userId);
    }

    public List<RuleLibrary> getRulesByType(Long userId, String type) {
        return ruleLibraryRepository.findByUserIdAndRuleType(userId, type);
    }

    public List<RuleLibrary> getActiveRules(Long userId) {
        return ruleLibraryRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public RuleLibrary getRuleById(Long id, Long userId) {
        return ruleLibraryRepository.findById(id)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("规则不存在"));
    }

    @Transactional
    public RuleLibrary createRule(RuleLibrary rule, Long userId) {
        rule.setUserId(userId);
        rule.setApplicationCount(0);
        rule.setIsActive(true);
        return ruleLibraryRepository.save(rule);
    }

    @Transactional
    public RuleLibrary updateRule(Long id, RuleLibrary rule, Long userId) {
        RuleLibrary existing = getRuleById(id, userId);
        existing.setRuleType(rule.getRuleType());
        existing.setRuleName(rule.getRuleName());
        existing.setRuleContent(rule.getRuleContent());
        existing.setRuleReason(rule.getRuleReason());
        existing.setViolationCases(rule.getViolationCases());
        existing.setPriority(rule.getPriority());
        existing.setIsActive(rule.getIsActive());
        return ruleLibraryRepository.save(existing);
    }

    @Transactional
    public RuleLibrary recordApplication(Long id, boolean success, Long userId) {
        RuleLibrary rule = getRuleById(id, userId);
        int newCount = rule.getApplicationCount() + 1;
        rule.setApplicationCount(newCount);
        
        BigDecimal currentRate = rule.getSuccessRate() != null ? rule.getSuccessRate() : BigDecimal.ZERO;
        BigDecimal newRate = currentRate.multiply(new BigDecimal(newCount - 1))
                .add(success ? new BigDecimal("100") : BigDecimal.ZERO)
                .divide(new BigDecimal(newCount), 2, RoundingMode.HALF_UP);
        rule.setSuccessRate(newRate);
        
        return ruleLibraryRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id, Long userId) {
        RuleLibrary rule = getRuleById(id, userId);
        ruleLibraryRepository.delete(rule);
    }
}
