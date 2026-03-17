package com.learning.notes.repository.finance;

import com.learning.notes.entity.finance.FinanceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceAccountRepository extends JpaRepository<FinanceAccount, Long> {
    List<FinanceAccount> findByUserId(Long userId);
    List<FinanceAccount> findByUserIdAndIsActiveTrue(Long userId);
    List<FinanceAccount> findByUserIdAndAccountType(Long userId, String accountType);
}
