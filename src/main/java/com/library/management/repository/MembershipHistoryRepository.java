package com.library.management.repository;

import com.library.management.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {

    List<MembershipHistory> findByMemberIdOrderByIdDesc(Long memberId);
}
