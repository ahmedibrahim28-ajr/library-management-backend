package com.library.management.service;

import com.library.management.dto.BookAvailabilityResponse;
import com.library.management.dto.BookRequest;
import com.library.management.dto.BookResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.enums.CopyStatus;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BookCopyRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookCopyRepository bookCopyRepository;

    public BookService(
            BookRepository bookRepository,
            CategoryRepository categoryRepository,
            BookCopyRepository bookCopyRepository
    ) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    public List<BookResponse> getAllBooks(String keyword) {

        List<Book> books;

        if (keyword == null || keyword.isBlank()) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
        }

        return books.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .filter(BookResponse::getIsAvailable)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        return mapToResponse(findBook(id));
    }

    public BookAvailabilityResponse getAvailability(Long id) {

        Book book = findBook(id);

        long total = bookCopyRepository.countByBookId(id);
        long available = bookCopyRepository.countByBookIdAndStatus(id, CopyStatus.AVAILABLE);
        long borrowed = bookCopyRepository.countByBookIdAndStatus(id, CopyStatus.BORROWED);

        BookAvailabilityResponse response = new BookAvailabilityResponse();

        response.setBookId(book.getId());
        response.setTitle(book.getTitle());
        response.setTotalCopies(total);
        response.setAvailableCopies(available);
        response.setBorrowedCopies(borrowed);
        response.setUnavailableCopies(total - available - borrowed);
        response.setIsAvailable(available > 0);

        return response;
    }

    public BookResponse createBook(BookRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("ISBN already exists");
        }

        Book book = new Book();

        applyRequest(book, request);

        return mapToResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(Long id, BookRequest request) {

        Book book = findBook(id);

        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("ISBN already exists");
        }

        applyRequest(book, request);

        return mapToResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        bookRepository.delete(findBook(id));
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private void applyRequest(Book book, BookRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        book.setCategory(category);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublicationYear(request.getPublicationYear());
        book.setLanguage(request.getLanguage());
        book.setDescription(request.getDescription());
    }

    private BookResponse mapToResponse(Book book) {

        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setPublisher(book.getPublisher());
        response.setPublicationYear(book.getPublicationYear());
        response.setLanguage(book.getLanguage());
        response.setDescription(book.getDescription());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getId());
            response.setCategoryName(book.getCategory().getName());
        }

        long available = bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE);

        response.setTotalCopies(bookCopyRepository.countByBookId(book.getId()));
        response.setAvailableCopies(available);
        response.setIsAvailable(available > 0);

        return response;
    }
}
