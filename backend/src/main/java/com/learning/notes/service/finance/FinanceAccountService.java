package com.learning.notes.service.finance;

import com.learning.notes.entity.finance.FinanceAccount;
import com.learning.notes.repository.finance.FinanceAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceAccountService {

    private final FinanceAccountRepository accountRepository;

    public List<FinanceAccount> getUserAccounts(Long userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<FinanceAccount> getAllUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public FinanceAccount getAccountById(Long id, Long userId) {
        return accountRepository.findById(id)
                .filter(account -> account.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("账户不存在"));
    }

    @Transactional
    public FinanceAccount createAccount(FinanceAccount account, Long userId) {
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    public FinanceAccount updateAccount(Long id, FinanceAccount account, Long userId) {
        FinanceAccount existing = getAccountById(id, userId);
        existing.setName(account.getName());
        existing.setAccountType(account.getAccountType());
        existing.setDescription(account.getDescription());
        existing.setRiskLevel(account.getRiskLevel());
        existing.setCurrency(account.getCurrency());
        return accountRepository.save(existing);
    }

    @Transactional
    public void deleteAccount(Long id, Long userId) {
        FinanceAccount account = getAccountById(id, userId);
        account.setIsActive(false);
        accountRepository.save(account);
    }

    public BigDecimal getTotalAssets(Long userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(FinanceAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
