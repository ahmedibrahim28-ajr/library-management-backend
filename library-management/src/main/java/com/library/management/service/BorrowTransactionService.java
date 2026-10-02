package com.library.management.service;

import com.library.management.dto.BorrowRequest;
import com.library.management.dto.BorrowTransactionResponse;
import com.library.management.entity.BookCopy;
import com.library.management.entity.BorrowTransaction;
import com.library.management.entity.Librarian;
import com.library.management.entity.Member;
import com.library.management.enums.CopyStatus;
import com.library.management.enums.MemberStatus;
import com.library.management.enums.TransactionStatus;
import com.library.management.exception.BusinessRuleException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BookCopyRepository;
import com.library.management.repository.BorrowTransactionRepository;
import com.library.management.repository.LibrarianRepository;
import com.library.management.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowTransactionRepository;
    private final MemberRepository memberRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LibrarianRepository librarianRepository;

    private final int loanDays;
    private final BigDecimal finePerDay;

    public BorrowTransactionService(
            BorrowTransactionRepository borrowTransactionRepository,
            MemberRepository memberRepository,
            BookCopyRepository bookCopyRepository,
            LibrarianRepository librarianRepository,
            @Value("${library.loan-days:14}") int loanDays,
            @Value("${library.fine-per-day:0.50}") BigDecimal finePerDay
    ) {
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.memberRepository = memberRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.librarianRepository = librarianRepository;
        this.loanDays = loanDays;
        this.finePerDay = finePerDay;
    }

    public List<BorrowTransactionResponse> getAllTransactions(Long memberId, TransactionStatus status) {

        List<BorrowTransaction> transactions;

        if (memberId != null && status != null) {
            transactions = borrowTransactionRepository.findByMemberIdAndStatus(memberId, status);
        } else if (memberId != null) {
            transactions = borrowTransactionRepository.findByMemberId(memberId);
        } else if (status != null) {
            transactions = borrowTransactionRepository.findByStatus(status);
        } else {
            transactions = borrowTransactionRepository.findAll();
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BorrowTransactionResponse> getOverdueTransactions() {
        return borrowTransactionRepository
                .findByStatusAndDueDateBefore(TransactionStatus.BORROWED, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BorrowTransactionResponse getTransactionById(Long id) {
        return mapToResponse(findTransaction(id));
    }

    @Transactional
    public BorrowTransactionResponse borrowBook(BorrowRequest request) {

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        BookCopy copy = bookCopyRepository.findById(request.getBookCopyId())
                .orElseThrow(() -> new ResourceNotFoundException("Book copy", request.getBookCopyId()));

        Librarian librarian = librarianRepository.findById(request.getLibrarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Librarian", request.getLibrarianId()));

        LocalDate today = LocalDate.now();

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessRuleException("Member is not active (status: " + member.getStatus() + ")");
        }

        if (member.getMembershipExpiry().isBefore(today)) {
            throw new BusinessRuleException("Membership expired on " + member.getMembershipExpiry());
        }

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BusinessRuleException("Book copy is not available (status: " + copy.getStatus() + ")");
        }

        long activeLoans = borrowTransactionRepository
                .countByMemberIdAndStatus(member.getId(), TransactionStatus.BORROWED);

        if (activeLoans >= member.getMaxBorrowLimit()) {
            throw new BusinessRuleException(
                    "Member reached the borrow limit of " + member.getMaxBorrowLimit() + " books"
            );
        }

        LocalDate dueDate = request.getDueDate() != null
                ? request.getDueDate()
                : today.plusDays(loanDays);

        if (!dueDate.isAfter(today)) {
            throw new BusinessRuleException("Due date must be after today");
        }

        BorrowTransaction transaction = new BorrowTransaction();

        transaction.setMember(member);
        transaction.setBookCopy(copy);
        transaction.setIssuedBy(librarian);
        transaction.setBorrowDate(today);
        transaction.setDueDate(dueDate);
        transaction.setStatus(TransactionStatus.BORROWED);
        transaction.setFineAmount(BigDecimal.ZERO);

        copy.setStatus(CopyStatus.BORROWED);
        bookCopyRepository.save(copy);

        return mapToResponse(borrowTransactionRepository.save(transaction));
    }

    @Transactional
    public BorrowTransactionResponse returnBook(Long id) {

        BorrowTransaction transaction = findTransaction(id);

        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            throw new BusinessRuleException("This book has already been returned");
        }

        LocalDate today = LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(transaction.getDueDate(), today);

        BigDecimal fine = daysLate > 0
                ? finePerDay.multiply(BigDecimal.valueOf(daysLate))
                : BigDecimal.ZERO;

        transaction.setReturnDate(today);
        transaction.setStatus(TransactionStatus.RETURNED);
        transaction.setFineAmount(fine);

        BookCopy copy = transaction.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        return mapToResponse(borrowTransactionRepository.save(transaction));
    }

    private BorrowTransaction findTransaction(Long id) {
        return borrowTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow transaction", id));
    }

    private BorrowTransactionResponse mapToResponse(BorrowTransaction transaction) {

        BorrowTransactionResponse response = new BorrowTransactionResponse();

        response.setId(transaction.getId());
        response.setBorrowDate(transaction.getBorrowDate());
        response.setDueDate(transaction.getDueDate());
        response.setReturnDate(transaction.getReturnDate());
        response.setStatus(transaction.getStatus());
        response.setFineAmount(transaction.getFineAmount());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        response.setOverdue(
                transaction.getStatus() == TransactionStatus.BORROWED
                        && transaction.getDueDate().isBefore(LocalDate.now())
        );

        if (transaction.getMember() != null) {
            response.setMemberId(transaction.getMember().getId());
            response.setMembershipNumber(transaction.getMember().getMembershipNumber());
            response.setMemberName(transaction.getMember().getUser().getName());
        }

        if (transaction.getBookCopy() != null) {
            response.setBookCopyId(transaction.getBookCopy().getId());
            response.setBarcode(transaction.getBookCopy().getBarcode());
            response.setBookId(transaction.getBookCopy().getBook().getId());
            response.setBookTitle(transaction.getBookCopy().getBook().getTitle());
        }

        if (transaction.getIssuedBy() != null) {
            response.setIssuedById(transaction.getIssuedBy().getId());
            response.setIssuedByName(transaction.getIssuedBy().getUser().getName());
        }

        return response;
    }
}
