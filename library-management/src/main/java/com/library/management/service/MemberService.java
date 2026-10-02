package com.library.management.service;

import com.library.management.dto.MemberRequest;
import com.library.management.dto.MemberResponse;
import com.library.management.dto.RenewMembershipRequest;
import com.library.management.entity.Member;
import com.library.management.entity.MembershipHistory;
import com.library.management.entity.User;
import com.library.management.enums.MemberStatus;
import com.library.management.enums.MembershipAction;
import com.library.management.enums.TransactionStatus;
import com.library.management.exception.BusinessRuleException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BorrowTransactionRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.repository.MembershipHistoryRepository;
import com.library.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;

    public MemberService(
            MemberRepository memberRepository,
            UserRepository userRepository,
            MembershipHistoryRepository membershipHistoryRepository,
            BorrowTransactionRepository borrowTransactionRepository
    ) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.membershipHistoryRepository = membershipHistoryRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
    }

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MemberResponse getMemberById(Long id) {
        return mapToResponse(findMember(id));
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (memberRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("This user is already a member");
        }

        if (memberRepository.existsByMembershipNumber(request.getMembershipNumber())) {
            throw new DuplicateResourceException("Membership number already exists");
        }

        LocalDate joinedAt = request.getJoinedAt() != null ? request.getJoinedAt() : LocalDate.now();

        if (!request.getMembershipExpiry().isAfter(joinedAt)) {
            throw new BusinessRuleException("Membership expiry must be after the join date");
        }

        Member member = new Member();

        member.setUser(user);
        member.setMembershipNumber(request.getMembershipNumber());
        member.setAddress(request.getAddress());
        member.setStatus(request.getStatus() != null ? request.getStatus() : MemberStatus.ACTIVE);
        member.setJoinedAt(joinedAt);
        member.setMembershipExpiry(request.getMembershipExpiry());
        member.setMaxBorrowLimit(request.getMaxBorrowLimit() != null ? request.getMaxBorrowLimit() : 5);

        Member savedMember = memberRepository.save(member);

        addHistory(
                savedMember,
                MembershipAction.JOINED,
                joinedAt,
                savedMember.getMembershipExpiry(),
                "Membership created"
        );

        return mapToResponse(savedMember);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request) {

        Member member = findMember(id);

        if (!member.getUser().getId().equals(request.getUserId())) {
            throw new BusinessRuleException("The user of a member cannot be changed");
        }

        if (!member.getMembershipNumber().equals(request.getMembershipNumber())
                && memberRepository.existsByMembershipNumber(request.getMembershipNumber())) {
            throw new DuplicateResourceException("Membership number already exists");
        }

        MemberStatus oldStatus = member.getStatus();
        MemberStatus newStatus = request.getStatus() != null ? request.getStatus() : oldStatus;
        LocalDate joinedAt = request.getJoinedAt() != null ? request.getJoinedAt() : member.getJoinedAt();

        if (!request.getMembershipExpiry().isAfter(joinedAt)) {
            throw new BusinessRuleException("Membership expiry must be after the join date");
        }

        member.setMembershipNumber(request.getMembershipNumber());
        member.setAddress(request.getAddress());
        member.setStatus(newStatus);
        member.setJoinedAt(joinedAt);
        member.setMembershipExpiry(request.getMembershipExpiry());

        if (request.getMaxBorrowLimit() != null) {
            member.setMaxBorrowLimit(request.getMaxBorrowLimit());
        }

        Member updatedMember = memberRepository.save(member);

        if (oldStatus != newStatus) {
            if (newStatus == MemberStatus.SUSPENDED) {
                addHistory(updatedMember, MembershipAction.SUSPENDED, LocalDate.now(), null, "Status changed manually");
            } else if (oldStatus == MemberStatus.SUSPENDED && newStatus == MemberStatus.ACTIVE) {
                addHistory(updatedMember, MembershipAction.REACTIVATED, LocalDate.now(),
                        updatedMember.getMembershipExpiry(), "Status changed manually");
            }
        }

        return mapToResponse(updatedMember);
    }

    @Transactional
    public MemberResponse renewMembership(Long id, RenewMembershipRequest request) {

        Member member = findMember(id);

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessRuleException("A suspended membership cannot be renewed; reactivate it first");
        }

        LocalDate today = LocalDate.now();
        LocalDate start = member.getMembershipExpiry().isAfter(today)
                ? member.getMembershipExpiry()
                : today;
        LocalDate newExpiry = start.plusMonths(request.getMonths());

        member.setMembershipExpiry(newExpiry);
        member.setStatus(MemberStatus.ACTIVE);

        Member renewedMember = memberRepository.save(member);

        addHistory(renewedMember, MembershipAction.RENEWED, start, newExpiry, request.getNotes());

        return mapToResponse(renewedMember);
    }

    public void deleteMember(Long id) {
        memberRepository.delete(findMember(id));
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    private void addHistory(
            Member member,
            MembershipAction action,
            LocalDate startDate,
            LocalDate endDate,
            String notes
    ) {
        MembershipHistory history = new MembershipHistory();

        history.setMember(member);
        history.setAction(action);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        history.setNotes(notes);

        membershipHistoryRepository.save(history);
    }

    private MemberResponse mapToResponse(Member member) {

        MemberResponse response = new MemberResponse();

        response.setId(member.getId());
        response.setMembershipNumber(member.getMembershipNumber());
        response.setAddress(member.getAddress());
        response.setStatus(member.getStatus());
        response.setJoinedAt(member.getJoinedAt());
        response.setMembershipExpiry(member.getMembershipExpiry());
        response.setMaxBorrowLimit(member.getMaxBorrowLimit());
        response.setCreatedAt(member.getCreatedAt());
        response.setUpdatedAt(member.getUpdatedAt());
        response.setActiveBorrowCount(
                borrowTransactionRepository.countByMemberIdAndStatus(member.getId(), TransactionStatus.BORROWED)
        );

        if (member.getUser() != null) {
            response.setUserId(member.getUser().getId());
            response.setUserName(member.getUser().getName());
            response.setUserEmail(member.getUser().getEmail());
        }

        return response;
    }
}
