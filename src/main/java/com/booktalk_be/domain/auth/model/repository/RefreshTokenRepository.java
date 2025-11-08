package com.booktalk_be.domain.auth.model.repository;

import com.booktalk_be.domain.auth.model.entity.Refresh_Token;
import com.booktalk_be.domain.auth.model.entity.Refresh_Token_id;
import com.booktalk_be.domain.member.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<Refresh_Token, Refresh_Token_id> {
    boolean existsByMember(Member member);

    Refresh_Token findByMember(Member member);

    void deleteByMember(Member member);
}
