package com.library.management.service;

import com.library.management.dto.LibrarianRequest;
import com.library.management.dto.LibrarianResponse;
import com.library.management.entity.Librarian;
import com.library.management.entity.User;
import com.library.management.exception.BusinessRuleException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.LibrarianRepository;
import com.library.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibrarianService {

    private final LibrarianRepository librarianRepository;
    private final UserRepository userRepository;

    public LibrarianService(
            LibrarianRepository librarianRepository,
            UserRepository userRepository
    ) {
        this.librarianRepository = librarianRepository;
        this.userRepository = userRepository;
    }

    public List<LibrarianResponse> getAllLibrarians() {
        return librarianRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LibrarianResponse getLibrarianById(Long id) {
        return mapToResponse(findLibrarian(id));
    }

    public LibrarianResponse createLibrarian(LibrarianRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (librarianRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("This user is already a librarian");
        }

        if (librarianRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee code already exists");
        }

        Librarian librarian = new Librarian();

        librarian.setUser(user);
        librarian.setEmployeeCode(request.getEmployeeCode());
        librarian.setHireDate(request.getHireDate());
        librarian.setSection(request.getSection());

        return mapToResponse(librarianRepository.save(librarian));
    }

    public LibrarianResponse updateLibrarian(Long id, LibrarianRequest request) {

        Librarian librarian = findLibrarian(id);

        if (!librarian.getUser().getId().equals(request.getUserId())) {
            throw new BusinessRuleException("The user of a librarian cannot be changed");
        }

        if (!librarian.getEmployeeCode().equals(request.getEmployeeCode())
                && librarianRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee code already exists");
        }

        librarian.setEmployeeCode(request.getEmployeeCode());
        librarian.setHireDate(request.getHireDate());
        librarian.setSection(request.getSection());

        return mapToResponse(librarianRepository.save(librarian));
    }

    public void deleteLibrarian(Long id) {
        librarianRepository.delete(findLibrarian(id));
    }

    private Librarian findLibrarian(Long id) {
        return librarianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian", id));
    }

    private LibrarianResponse mapToResponse(Librarian librarian) {

        LibrarianResponse response = new LibrarianResponse();

        response.setId(librarian.getId());
        response.setEmployeeCode(librarian.getEmployeeCode());
        response.setHireDate(librarian.getHireDate());
        response.setSection(librarian.getSection());
        response.setCreatedAt(librarian.getCreatedAt());

        if (librarian.getUser() != null) {
            response.setUserId(librarian.getUser().getId());
            response.setUserName(librarian.getUser().getName());
            response.setUserEmail(librarian.getUser().getEmail());
        }

        return response;
    }
}
