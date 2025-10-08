package com.booktalk_be.domain.member.service;


import com.booktalk_be.domain.member.command.CreateMemberCommand;
import com.booktalk_be.domain.member.command.ModifyMemberCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import com.booktalk_be.domain.member.responseDto.MemberInformationResponse;
import com.booktalk_be.domain.reply.model.entity.Reply;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member getMemberById (int id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    }

    public Member createMember(CreateMemberCommand memberDTO) {
        Member member = memberDTO.toEntity(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

    public Member modifyMember(ModifyMemberCommand memberDTO, Authentication authentication) {
        Member authMember = (Member) authentication.getPrincipal();
        authMember.modify(memberDTO);
        System.out.println(authMember.getAddress());
        memberRepository.save(authMember);
        return authMember;
    }

    //중복 체크를 위해 정의
    public Boolean validationEmail (String email) {
        return memberRepository.existsMembersByEmail(email);
    }

    public MemberInformationResponse getAuthenticationMember (Member member) {
        return MemberInformationResponse.builder().
                name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .address(member.getAddress())
                .birth(member.getBirth())
                .authType(member.getAuthType())
                .gender(member.getGender())
                .build();
    }

    //Authentication 객체 호출 용, 시큐리티 컨텍스트에 저장된 정보를 얻기 위함
//    private Authentication getAuthentication() {
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
//            throw new AuthenticationCredentialsNotFoundException("등록된 사용자 컨텍스트 정보 없음");
//        }
//        return authentication;
//    }
//
//    //Authentication 객체 호출 후 member entity 리턴
//    public Member getCurrentUser() {
//        Authentication authentication = getAuthentication();
//        return (Member) authentication.getPrincipal();
//    }
}
