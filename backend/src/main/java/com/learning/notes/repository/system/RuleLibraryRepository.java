package com.learning.notes.repository.system;

import com.learning.notes.entity.system.RuleLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleLibraryRepository extends JpaRepository<RuleLibrary, Long> {
    List<RuleLibrary> findByUserId(Long userId);
    List<RuleLibrary> findByUserIdAndRuleType(Long userId, String ruleType);
    List<RuleLibrary> findByUserIdAndIsActiveTrue(Long userId);
}
