package com.library.management.dto;

import com.library.management.enums.MemberStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class MemberRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 30)
    private String membershipNumber;

    @Size(max = 255)
    private String address;

    private MemberStatus status = MemberStatus.ACTIVE;

    private LocalDate joinedAt;

    @NotNull
    private LocalDate membershipExpiry;

    @Min(1)
    @Max(20)
    private Integer maxBorrowLimit = 5;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public LocalDate getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDate joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDate getMembershipExpiry() {
        return membershipExpiry;
    }

    public void setMembershipExpiry(LocalDate membershipExpiry) {
        this.membershipExpiry = membershipExpiry;
    }

    public Integer getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(Integer maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }
}
