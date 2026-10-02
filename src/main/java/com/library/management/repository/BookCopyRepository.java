package com.library.management.repository;

import com.library.management.entity.BookCopy;
import com.library.management.enums.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    boolean existsByBarcode(String barcode);

    List<BookCopy> findByBookId(Long bookId);

    List<BookCopy> findByStatus(CopyStatus status);

    List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);

    long countByBookId(Long bookId);

    long countByBookIdAndStatus(Long bookId, CopyStatus status);
}
