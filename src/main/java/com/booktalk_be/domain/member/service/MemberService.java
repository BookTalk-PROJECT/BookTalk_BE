package com.booktalk_be.domain.member.service;


import com.booktalk_be.domain.member.command.CreateMemberCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member getMemberById (String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    }
    
    //일단 테스트로 해보고 중복체크도 할 예정
    public Member createMember(CreateMemberCommand memberDTO) {
        Member member = memberDTO.toEntity(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

    public Boolean validationEmail (String email) {

        return memberRepository.existsMembersByEmail(email);
    }
}
