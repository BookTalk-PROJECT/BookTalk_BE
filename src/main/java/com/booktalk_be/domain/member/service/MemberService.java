package com.booktalk_be.domain.member.service;


import com.booktalk_be.domain.auth.model.entity.AuthorityType;
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

import java.util.List;
import java.util.stream.Collectors;


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

    public List<MemberInformationResponse> getMemberAllList () {
        List<MemberInformationResponse> allMemberList = memberRepository.findAll()
                .stream()
                .map(MemberInformationResponse::new)
                .toList();
        if(allMemberList.isEmpty()){
            throw new EntityNotFoundException("member list is empty.");
        }
        return allMemberList;
    }

    public Member createMember(CreateMemberCommand memberDTO) {
        Member member = memberDTO.toEntity(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

    public Member modifyMember(ModifyMemberCommand memberDTO, Authentication authentication) {
        Member authMember = (Member) authentication.getPrincipal();
        String encordPassword = bCryptPasswordEncoder.encode(memberDTO.getPassword());
        authMember.modify(memberDTO, encordPassword);
        memberRepository.save(authMember);
        return authMember;
    }

    //중복 체크를 위해 정의
    public Boolean validationEmail (String email) {
        return memberRepository.existsMembersByEmail(email);
    }

    public MemberInformationResponse getAuthenticationMember (Member member) {
        return new MemberInformationResponse(member);
    }

    public Member modifyRole(String id, String role) {
        Member member = getMemberById(Integer.parseInt(id));
        member.modifyRole(AuthorityType.valueOf(role));
        return memberRepository.save(member);
    }
    }
