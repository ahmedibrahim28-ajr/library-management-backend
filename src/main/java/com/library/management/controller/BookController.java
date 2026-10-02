package com.library.management.controller;

import com.library.management.dto.BookAvailabilityResponse;
import com.library.management.dto.BookRequest;
import com.library.management.dto.BookResponse;
import com.library.management.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Optional search: GET /api/books?keyword=orwell
    @GetMapping
    public List<BookResponse> getAllBooks(
            @RequestParam(required = false) String keyword
    ) {
        return bookService.getAllBooks(keyword);
    }

    @GetMapping("/available")
    public List<BookResponse> getAvailableBooks() {
        return bookService.getAvailableBooks();
    }

    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/{id}/availability")
    public BookAvailabilityResponse getAvailability(@PathVariable Long id) {
        return bookService.getAvailability(id);
    }

    @PostMapping
    public BookResponse createBook(
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.createBook(request);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
