package com.library.management.repository;

import com.library.management.entity.Librarian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibrarianRepository extends JpaRepository<Librarian, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);
}
