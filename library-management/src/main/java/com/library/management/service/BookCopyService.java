package com.library.management.service;

import com.library.management.dto.BookCopyRequest;
import com.library.management.dto.BookCopyResponse;
import com.library.management.entity.Book;
import com.library.management.entity.BookCopy;
import com.library.management.enums.CopyStatus;
import com.library.management.exception.BusinessRuleException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BookCopyRepository;
import com.library.management.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    public BookCopyService(
            BookCopyRepository bookCopyRepository,
            BookRepository bookRepository
    ) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
    }

    public List<BookCopyResponse> getAllCopies(Long bookId, CopyStatus status) {

        List<BookCopy> copies;

        if (bookId != null && status != null) {
            copies = bookCopyRepository.findByBookIdAndStatus(bookId, status);
        } else if (bookId != null) {
            copies = bookCopyRepository.findByBookId(bookId);
        } else if (status != null) {
            copies = bookCopyRepository.findByStatus(status);
        } else {
            copies = bookCopyRepository.findAll();
        }

        return copies.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookCopyResponse getCopyById(Long id) {
        return mapToResponse(findCopy(id));
    }

    public BookCopyResponse createCopy(BookCopyRequest request) {

        if (bookCopyRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists");
        }

        if (request.getStatus() == CopyStatus.BORROWED) {
            throw new BusinessRuleException(
                    "A new copy cannot be BORROWED; use the borrow transaction endpoint"
            );
        }

        Book book = findBook(request.getBookId());

        BookCopy copy = new BookCopy();

        copy.setBook(book);
        copy.setBarcode(request.getBarcode());
        copy.setShelfLocation(request.getShelfLocation());
        copy.setStatus(request.getStatus() != null ? request.getStatus() : CopyStatus.AVAILABLE);
        copy.setAcquiredAt(request.getAcquiredAt() != null ? request.getAcquiredAt() : LocalDate.now());

        return mapToResponse(bookCopyRepository.save(copy));
    }

    public BookCopyResponse updateCopy(Long id, BookCopyRequest request) {

        BookCopy copy = findCopy(id);

        if (!copy.getBarcode().equals(request.getBarcode())
                && bookCopyRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists");
        }

        CopyStatus newStatus = request.getStatus() != null ? request.getStatus() : copy.getStatus();

        // BORROWED / AVAILABLE transitions belong to the borrow and return operations
        if (newStatus != copy.getStatus()
                && (newStatus == CopyStatus.BORROWED || copy.getStatus() == CopyStatus.BORROWED)) {
            throw new BusinessRuleException(
                    "Status BORROWED is managed by borrow and return transactions"
            );
        }

        copy.setBook(findBook(request.getBookId()));
        copy.setBarcode(request.getBarcode());
        copy.setShelfLocation(request.getShelfLocation());
        copy.setStatus(newStatus);

        if (request.getAcquiredAt() != null) {
            copy.setAcquiredAt(request.getAcquiredAt());
        }

        return mapToResponse(bookCopyRepository.save(copy));
    }

    public void deleteCopy(Long id) {
        bookCopyRepository.delete(findCopy(id));
    }

    private BookCopy findCopy(Long id) {
        return bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy", id));
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private BookCopyResponse mapToResponse(BookCopy copy) {

        BookCopyResponse response = new BookCopyResponse();

        response.setId(copy.getId());
        response.setBarcode(copy.getBarcode());
        response.setShelfLocation(copy.getShelfLocation());
        response.setStatus(copy.getStatus());
        response.setAcquiredAt(copy.getAcquiredAt());
        response.setCreatedAt(copy.getCreatedAt());
        response.setUpdatedAt(copy.getUpdatedAt());

        if (copy.getBook() != null) {
            response.setBookId(copy.getBook().getId());
            response.setBookTitle(copy.getBook().getTitle());
        }

        return response;
    }
}
