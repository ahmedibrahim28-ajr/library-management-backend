package com.library.management.service;

import com.library.management.dto.MembershipHistoryResponse;
import com.library.management.entity.MembershipHistory;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.MemberRepository;
import com.library.management.repository.MembershipHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipHistoryService {

    private final MembershipHistoryRepository membershipHistoryRepository;
    private final MemberRepository memberRepository;

    public MembershipHistoryService(
            MembershipHistoryRepository membershipHistoryRepository,
            MemberRepository memberRepository
    ) {
        this.membershipHistoryRepository = membershipHistoryRepository;
        this.memberRepository = memberRepository;
    }

    public List<MembershipHistoryResponse> getAllHistory() {
        return membershipHistoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<MembershipHistoryResponse> getHistoryByMember(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }

        return membershipHistoryRepository.findByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MembershipHistoryResponse mapToResponse(MembershipHistory history) {

        MembershipHistoryResponse response = new MembershipHistoryResponse();

        response.setId(history.getId());
        response.setAction(history.getAction());
        response.setStartDate(history.getStartDate());
        response.setEndDate(history.getEndDate());
        response.setNotes(history.getNotes());
        response.setCreatedAt(history.getCreatedAt());

        if (history.getMember() != null) {
            response.setMemberId(history.getMember().getId());
            response.setMemberName(history.getMember().getUser().getName());
        }

        return response;
    }
}
