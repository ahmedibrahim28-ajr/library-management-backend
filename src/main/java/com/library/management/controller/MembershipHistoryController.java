package com.library.management.controller;

import com.library.management.dto.MembershipHistoryResponse;
import com.library.management.service.MembershipHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-history")
public class MembershipHistoryController {

    private final MembershipHistoryService membershipHistoryService;

    public MembershipHistoryController(MembershipHistoryService membershipHistoryService) {
        this.membershipHistoryService = membershipHistoryService;
    }

    @GetMapping
    public List<MembershipHistoryResponse> getAllHistory() {
        return membershipHistoryService.getAllHistory();
    }

    @GetMapping("/member/{memberId}")
    public List<MembershipHistoryResponse> getHistoryByMember(@PathVariable Long memberId) {
        return membershipHistoryService.getHistoryByMember(memberId);
    }
}
