package com.booktalk_be.domain.member.mypage.model.repository;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByEmail(String email);
}
