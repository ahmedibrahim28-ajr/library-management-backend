package com.library.management.controller;

import com.library.management.dto.BookCopyRequest;
import com.library.management.dto.BookCopyResponse;
import com.library.management.enums.CopyStatus;
import com.library.management.service.BookCopyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
public class BookCopyController {

    private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    // Optional filters: GET /api/book-copies?bookId=1&status=AVAILABLE
    @GetMapping
    public List<BookCopyResponse> getAllCopies(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) CopyStatus status
    ) {
        return bookCopyService.getAllCopies(bookId, status);
    }

    @GetMapping("/{id}")
    public BookCopyResponse getCopyById(@PathVariable Long id) {
        return bookCopyService.getCopyById(id);
    }

    @PostMapping
    public BookCopyResponse createCopy(
            @Valid @RequestBody BookCopyRequest request
    ) {
        return bookCopyService.createCopy(request);
    }

    @PutMapping("/{id}")
    public BookCopyResponse updateCopy(
            @PathVariable Long id,
            @Valid @RequestBody BookCopyRequest request
    ) {
        return bookCopyService.updateCopy(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCopy(@PathVariable Long id) {
        bookCopyService.deleteCopy(id);
    }
}
