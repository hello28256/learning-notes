package com.learning.notes.repository.finance;

import com.learning.notes.entity.finance.FinancePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancePositionRepository extends JpaRepository<FinancePosition, Long> {
    List<FinancePosition> findByUserId(Long userId);
    List<FinancePosition> findByUserIdAndIsActiveTrue(Long userId);
    List<FinancePosition> findByUserIdAndAccountId(Long userId, Long accountId);
    Optional<FinancePosition> findByUserIdAndSymbolAndIsActiveTrue(Long userId, String symbol);
}
