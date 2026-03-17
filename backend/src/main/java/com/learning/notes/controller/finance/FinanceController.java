package com.learning.notes.controller.finance;

import com.learning.notes.entity.finance.*;
import com.learning.notes.service.finance.*;
import com.learning.notes.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceAccountService accountService;
    private final FinanceTransactionService transactionService;
    private final FinanceDecisionService decisionService;
    private final FinanceMistakeService mistakeService;

    // ==================== 查询接口（公开访问）====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getFinanceDashboard() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> dashboard = new HashMap<>();
        
        List<FinanceAccount> accounts = accountService.getUserAccounts(userId);
        BigDecimal totalAssets = accountService.getTotalAssets(userId);
        List<FinanceDecision> pendingReviews = decisionService.getPendingReviewDecisions(userId);
        
        dashboard.put("accounts", accounts);
        dashboard.put("totalAssets", totalAssets);
        dashboard.put("accountCount", accounts.size());
        dashboard.put("pendingReviews", pendingReviews);
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<FinanceAccount>> getAccounts() {
        return ResponseEntity.ok(accountService.getUserAccounts(UserContext.getCurrentUserId()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<FinanceTransaction>> getTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = UserContext.getCurrentUserId();
        
        if (accountId != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByAccount(userId, accountId));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByDateRange(
                    userId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
        }
        return ResponseEntity.ok(transactionService.getUserTransactions(userId));
    }

    @GetMapping("/decisions")
    public ResponseEntity<List<FinanceDecision>> getDecisions(@RequestParam(required = false) String type) {
        Long userId = UserContext.getCurrentUserId();
        if (type != null) {
            return ResponseEntity.ok(decisionService.getDecisionsByType(userId, type));
        }
        return ResponseEntity.ok(decisionService.getUserDecisions(userId));
    }

    @GetMapping("/decisions/pending-review")
    public ResponseEntity<List<FinanceDecision>> getPendingReviewDecisions() {
        return ResponseEntity.ok(decisionService.getPendingReviewDecisions(UserContext.getCurrentUserId()));
    }

    @GetMapping("/mistakes")
    public ResponseEntity<List<FinanceMistake>> getMistakes(@RequestParam(required = false) String category) {
        Long userId = UserContext.getCurrentUserId();
        if (category != null) {
            return ResponseEntity.ok(mistakeService.getMistakesByCategory(userId, category));
        }
        return ResponseEntity.ok(mistakeService.getUserMistakes(userId));
    }

    // ==================== 修改接口（需要登录）====================

    @PostMapping("/accounts")
    public ResponseEntity<FinanceAccount> createAccount(@RequestBody FinanceAccount account) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(accountService.createAccount(account, UserContext.getCurrentUserId()));
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<FinanceAccount> updateAccount(@PathVariable Long id, @RequestBody FinanceAccount account) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(accountService.updateAccount(id, account, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        accountService.deleteAccount(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transactions")
    public ResponseEntity<FinanceTransaction> createTransaction(@RequestBody FinanceTransaction transaction) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(transactionService.createTransaction(transaction, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        transactionService.deleteTransaction(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decisions")
    public ResponseEntity<FinanceDecision> createDecision(@RequestBody FinanceDecision decision) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(decisionService.createDecision(decision, UserContext.getCurrentUserId()));
    }

    @PutMapping("/decisions/{id}")
    public ResponseEntity<FinanceDecision> updateDecision(@PathVariable Long id, @RequestBody FinanceDecision decision) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(decisionService.updateDecision(id, decision, UserContext.getCurrentUserId()));
    }

    @PostMapping("/decisions/{id}/review")
    public ResponseEntity<FinanceDecision> reviewDecision(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam String notes) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(decisionService.reviewDecision(id, result, notes, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/decisions/{id}")
    public ResponseEntity<Void> deleteDecision(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        decisionService.deleteDecision(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mistakes")
    public ResponseEntity<FinanceMistake> createMistake(@RequestBody FinanceMistake mistake) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mistakeService.createMistake(mistake, UserContext.getCurrentUserId()));
    }

    @PutMapping("/mistakes/{id}")
    public ResponseEntity<FinanceMistake> updateMistake(@PathVariable Long id, @RequestBody FinanceMistake mistake) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mistakeService.updateMistake(id, mistake, UserContext.getCurrentUserId()));
    }

    @PostMapping("/mistakes/{id}/review")
    public ResponseEntity<FinanceMistake> reviewMistake(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mistakeService.markAsReviewed(id, UserContext.getCurrentUserId()));
    }

    @DeleteMapping("/mistakes/{id}")
    public ResponseEntity<Void> deleteMistake(@PathVariable Long id) {
        if (!UserContext.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        mistakeService.deleteMistake(id, UserContext.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
