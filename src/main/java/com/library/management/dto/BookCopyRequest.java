package com.library.management.dto;

import com.library.management.enums.CopyStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookCopyRequest {

    @NotNull
    private Long bookId;

    @NotBlank
    @Size(max = 50)
    private String barcode;

    @Size(max = 50)
    private String shelfLocation;

    private CopyStatus status = CopyStatus.AVAILABLE;

    private LocalDate acquiredAt;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
    }
}
