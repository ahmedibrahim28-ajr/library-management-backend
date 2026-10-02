package com.library.management.controller;

import com.library.management.dto.LibrarianRequest;
import com.library.management.dto.LibrarianResponse;
import com.library.management.service.LibrarianService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/librarians")
public class LibrarianController {

    private final LibrarianService librarianService;

    public LibrarianController(LibrarianService librarianService) {
        this.librarianService = librarianService;
    }

    @GetMapping
    public List<LibrarianResponse> getAllLibrarians() {
        return librarianService.getAllLibrarians();
    }

    @GetMapping("/{id}")
    public LibrarianResponse getLibrarianById(@PathVariable Long id) {
        return librarianService.getLibrarianById(id);
    }

    @PostMapping
    public LibrarianResponse createLibrarian(
            @Valid @RequestBody LibrarianRequest request
    ) {
        return librarianService.createLibrarian(request);
    }

    @PutMapping("/{id}")
    public LibrarianResponse updateLibrarian(
            @PathVariable Long id,
            @Valid @RequestBody LibrarianRequest request
    ) {
        return librarianService.updateLibrarian(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteLibrarian(@PathVariable Long id) {
        librarianService.deleteLibrarian(id);
    }
}
