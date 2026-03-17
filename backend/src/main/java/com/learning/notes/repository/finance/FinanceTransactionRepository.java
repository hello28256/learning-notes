package com.learning.notes.repository.finance;

import com.learning.notes.entity.finance.FinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {
    List<FinanceTransaction> findByUserId(Long userId);
    List<FinanceTransaction> findByUserIdAndAccountId(Long userId, Long accountId);
    List<FinanceTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<FinanceTransaction> findByUserIdAndTransactionType(Long userId, String transactionType);

    @Query("SELECT SUM(t.amount) FROM FinanceTransaction t WHERE t.userId = :userId AND t.transactionType = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(@Param("userId") Long userId, @Param("type") String type, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
