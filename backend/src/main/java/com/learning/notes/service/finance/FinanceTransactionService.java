package com.learning.notes.service.finance;

import com.learning.notes.entity.finance.FinanceAccount;
import com.learning.notes.entity.finance.FinancePosition;
import com.learning.notes.entity.finance.FinanceTransaction;
import com.learning.notes.repository.finance.FinanceAccountRepository;
import com.learning.notes.repository.finance.FinancePositionRepository;
import com.learning.notes.repository.finance.FinanceTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinanceTransactionService {

    private final FinanceTransactionRepository transactionRepository;
    private final FinanceAccountRepository accountRepository;
    private final FinancePositionRepository positionRepository;

    public List<FinanceTransaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<FinanceTransaction> getTransactionsByAccount(Long userId, Long accountId) {
        return transactionRepository.findByUserIdAndAccountId(userId, accountId);
    }

    public List<FinanceTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }

    @Transactional
    public FinanceTransaction createTransaction(FinanceTransaction transaction, Long userId) {
        transaction.setUserId(userId);
        FinanceTransaction saved = transactionRepository.save(transaction);
        
        updateAccountBalance(transaction);
        
        if ("buy".equals(transaction.getTransactionType()) || "sell".equals(transaction.getTransactionType())) {
            updatePosition(transaction);
        }
        
        return saved;
    }

    private void updateAccountBalance(FinanceTransaction transaction) {
        FinanceAccount account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        BigDecimal amount = transaction.getAmount();
        BigDecimal fee = transaction.getFee() != null ? transaction.getFee() : BigDecimal.ZERO;
        
        switch (transaction.getTransactionType()) {
            case "deposit":
            case "dividend":
                account.setBalance(account.getBalance().add(amount));
                break;
            case "withdraw":
            case "buy":
                account.setBalance(account.getBalance().subtract(amount).subtract(fee));
                break;
            case "sell":
                account.setBalance(account.getBalance().add(amount).subtract(fee));
                break;
        }
        
        accountRepository.save(account);
    }

    private void updatePosition(FinanceTransaction transaction) {
        String symbol = transaction.getSymbol();
        if (symbol == null) return;
        
        Optional<FinancePosition> existingPosition = positionRepository
                .findByUserIdAndSymbolAndIsActiveTrue(transaction.getUserId(), symbol);
        
        if ("buy".equals(transaction.getTransactionType())) {
            if (existingPosition.isPresent()) {
                FinancePosition position = existingPosition.get();
                BigDecimal totalCost = position.getAvgCost().multiply(position.getQuantity())
                        .add(transaction.getPrice().multiply(transaction.getQuantity()));
                BigDecimal totalQty = position.getQuantity().add(transaction.getQuantity());
                position.setAvgCost(totalCost.divide(totalQty, 4, RoundingMode.HALF_UP));
                position.setQuantity(totalQty);
                positionRepository.save(position);
            } else {
                FinancePosition newPosition = FinancePosition.builder()
                        .userId(transaction.getUserId())
                        .accountId(transaction.getAccountId())
                        .symbol(symbol)
                        .positionType("stock")
                        .quantity(transaction.getQuantity())
                        .avgCost(transaction.getPrice())
                        .isActive(true)
                        .build();
                positionRepository.save(newPosition);
            }
        } else if ("sell".equals(transaction.getTransactionType())) {
            existingPosition.ifPresent(position -> {
                BigDecimal newQty = position.getQuantity().subtract(transaction.getQuantity());
                if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
                    position.setIsActive(false);
                    position.setQuantity(BigDecimal.ZERO);
                } else {
                    position.setQuantity(newQty);
                }
                
                BigDecimal realizedPnl = transaction.getPrice().subtract(position.getAvgCost())
                        .multiply(transaction.getQuantity());
                position.setRealizedPnl(position.getRealizedPnl().add(realizedPnl));
                
                positionRepository.save(position);
            });
        }
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        FinanceTransaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("交易记录不存在"));
        
        transactionRepository.delete(transaction);
    }
}
