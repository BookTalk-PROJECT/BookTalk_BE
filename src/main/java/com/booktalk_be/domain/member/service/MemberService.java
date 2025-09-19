package com.booktalk_be.domain.member.service;


import com.booktalk_be.domain.member.command.CreateMemberCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member getMemberById (int id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    }
    
    //일단 테스트로 해보고 중복체크도 할 예정
    public Member createMember(CreateMemberCommand memberDTO) {
        Member member = memberDTO.toEntity(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

    //중복 체크를 위해 정의
    public Boolean validationEmail (String email) {
        return memberRepository.existsMembersByEmail(email);
    }

    //Authentication 객체 호출 용, 시큐리티 컨텍스트에 저장된 정보를 얻기 위함
    private Authentication getAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("등록된 사용자 컨텍스트 정보 없음");
        }
        return authentication;
    }

    //Authentication 객체 호출 후 member entity 리턴
    public Member getCurrentUser() {
        Authentication authentication = getAuthentication();
        return (Member) authentication.getPrincipal();
    }
}
