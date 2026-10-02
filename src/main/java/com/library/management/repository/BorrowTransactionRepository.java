package com.library.management.repository;

import com.library.management.entity.BorrowTransaction;
import com.library.management.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    List<BorrowTransaction> findByMemberId(Long memberId);

    List<BorrowTransaction> findByStatus(TransactionStatus status);

    List<BorrowTransaction> findByMemberIdAndStatus(Long memberId, TransactionStatus status);

    List<BorrowTransaction> findByStatusAndDueDateBefore(TransactionStatus status, LocalDate date);

    long countByMemberIdAndStatus(Long memberId, TransactionStatus status);
}
