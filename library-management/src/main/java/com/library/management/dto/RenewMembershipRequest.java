package com.library.management.dto;

import jakarta.validation.constraints.*;

public class RenewMembershipRequest {

    @NotNull
    @Min(1)
    @Max(36)
    private Integer months;

    @Size(max = 255)
    private String notes;

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
