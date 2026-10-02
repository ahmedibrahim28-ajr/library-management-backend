package com.library.management.dto;


public class BookAvailabilityResponse {

    private Long bookId;

    private String title;

    private Long totalCopies;

    private Long availableCopies;

    private Long borrowedCopies;

    private Long unavailableCopies;

    private Boolean isAvailable;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Long totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Long getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Long availableCopies) {
        this.availableCopies = availableCopies;
    }

    public Long getBorrowedCopies() {
        return borrowedCopies;
    }

    public void setBorrowedCopies(Long borrowedCopies) {
        this.borrowedCopies = borrowedCopies;
    }

    public Long getUnavailableCopies() {
        return unavailableCopies;
    }

    public void setUnavailableCopies(Long unavailableCopies) {
        this.unavailableCopies = unavailableCopies;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
