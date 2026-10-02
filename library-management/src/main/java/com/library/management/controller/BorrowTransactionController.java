package com.library.management.controller;

import com.library.management.dto.BorrowRequest;
import com.library.management.dto.BorrowTransactionResponse;
import com.library.management.enums.TransactionStatus;
import com.library.management.service.BorrowTransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-transactions")
public class BorrowTransactionController {

    private final BorrowTransactionService borrowTransactionService;

    public BorrowTransactionController(BorrowTransactionService borrowTransactionService) {
        this.borrowTransactionService = borrowTransactionService;
    }

    // Optional filters: GET /api/borrow-transactions?memberId=1&status=BORROWED
    @GetMapping
    public List<BorrowTransactionResponse> getAllTransactions(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) TransactionStatus status
    ) {
        return borrowTransactionService.getAllTransactions(memberId, status);
    }

    @GetMapping("/overdue")
    public List<BorrowTransactionResponse> getOverdueTransactions() {
        return borrowTransactionService.getOverdueTransactions();
    }

    @GetMapping("/{id}")
    public BorrowTransactionResponse getTransactionById(@PathVariable Long id) {
        return borrowTransactionService.getTransactionById(id);
    }

    @PostMapping
    public BorrowTransactionResponse borrowBook(
            @Valid @RequestBody BorrowRequest request
    ) {
        return borrowTransactionService.borrowBook(request);
    }

    @PutMapping("/{id}/return")
    public BorrowTransactionResponse returnBook(@PathVariable Long id) {
        return borrowTransactionService.returnBook(id);
    }
}
