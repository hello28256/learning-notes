package com.learning.notes.controller.system;

import com.learning.notes.entity.system.MentalModel;
import com.learning.notes.entity.system.RuleLibrary;
import com.learning.notes.service.system.MentalModelService;
import com.learning.notes.service.system.RuleLibraryService;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final MentalModelService mentalModelService;
    private final RuleLibraryService ruleLibraryService;

    // ==================== 查询接口（公开访问）====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getSystemDashboard() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> dashboard = new HashMap<>();

        List<MentalModel> mentalModels = mentalModelService.getActiveModels(userId);
        List<RuleLibrary> rules = ruleLibraryService.getActiveRules(userId);

        dashboard.put("mentalModels", mentalModels);
        dashboard.put("rules", rules);
        dashboard.put("mentalModelCount", mentalModels.size());
        dashboard.put("ruleCount", rules.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/mental-models")
    public ResponseEntity<List<MentalModel>> getMentalModels() {
        return ResponseEntity.ok(mentalModelService.getUserModels(UserContext.getCurrentUserId()));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<RuleLibrary>> getRules(@RequestParam(required = false) String type) {
        Long userId = UserContext.getCurrentUserId();
        if (type != null) {
            return ResponseEntity.ok(ruleLibraryService.getRulesByType(userId, type));
        }
        return ResponseEntity.ok(ruleLibraryService.getUserRules(userId));
    }

    // ==================== 修改接口（需要登录）====================

    @PostMapping("/mental-models")
    public ResponseEntity<MentalModel> createMentalModel(@RequestBody MentalModel model) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mentalModelService.createModel(model, UserContext.getCurrentUserId()));
    }

    @PutMapping("/mental-models/{id}")
    public ResponseEntity<MentalModel> updateMentalModel(@PathVariable Long id, @RequestBody MentalModel model) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mentalModelService.updateModel(id, model, UserContext.getCurrentUserId()));
    }

    @PostMapping("/mental-models/{id}/use")
    public ResponseEntity<MentalModel> incrementModelUsage(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mentalModelService.incrementUsage(id, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/mental-models/{id}")
    public ResponseEntity<Void> deleteMentalModel(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        mentalModelService.deleteModel(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rules")
    public ResponseEntity<RuleLibrary> createRule(@RequestBody RuleLibrary rule) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(ruleLibraryService.createRule(rule, UserContext.getCurrentUserId()));
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<RuleLibrary> updateRule(@PathVariable Long id, @RequestBody RuleLibrary rule) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(ruleLibraryService.updateRule(id, rule, UserContext.getCurrentUserId()));
    }

    @PostMapping("/rules/{id}/apply")
    public ResponseEntity<RuleLibrary> applyRule(@PathVariable Long id, @RequestParam boolean success) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(ruleLibraryService.recordApplication(id, success, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        ruleLibraryService.deleteRule(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
