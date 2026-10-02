package com.library.management.dto;

import jakarta.validation.constraints.*;

public class BookRequest {

    @NotNull
    private Integer categoryId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 150)
    private String author;

    @NotBlank
    @Size(max = 20)
    private String isbn;

    @Size(max = 150)
    private String publisher;

    @Min(1000)
    @Max(2100)
    private Integer publicationYear;

    @Size(max = 30)
    private String language;

    private String description;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
