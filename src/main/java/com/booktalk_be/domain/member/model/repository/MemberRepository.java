package com.booktalk_be.domain.member.model.repository;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.member.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> ,MemberRepositoryCustom{
    Optional<Member> findByEmail(String email);

    Boolean existsMembersByEmail(String email);

    Optional<Member> findByEmailAndAuthType(String email, AuthenticateType authType);


}
