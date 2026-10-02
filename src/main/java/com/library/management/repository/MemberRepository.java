package com.library.management.repository;

import com.library.management.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByMembershipNumber(String membershipNumber);
}
