package com.library.management.controller;

import com.library.management.dto.MemberRequest;
import com.library.management.dto.MemberResponse;
import com.library.management.dto.RenewMembershipRequest;
import com.library.management.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public MemberResponse getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @PostMapping
    public MemberResponse createMember(
            @Valid @RequestBody MemberRequest request
    ) {
        return memberService.createMember(request);
    }

    @PutMapping("/{id}")
    public MemberResponse updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request
    ) {
        return memberService.updateMember(id, request);
    }

    @PostMapping("/{id}/renew")
    public MemberResponse renewMembership(
            @PathVariable Long id,
            @Valid @RequestBody RenewMembershipRequest request
    ) {
        return memberService.renewMembership(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }
}
