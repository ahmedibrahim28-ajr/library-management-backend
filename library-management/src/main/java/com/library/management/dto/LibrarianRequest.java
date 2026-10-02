package com.library.management.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class LibrarianRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 30)
    private String employeeCode;

    @NotNull
    private LocalDate hireDate;

    @Size(max = 100)
    private String section;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
