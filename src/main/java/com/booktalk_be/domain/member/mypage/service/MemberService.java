package com.booktalk_be.domain.member.mypage.service;


import com.booktalk_be.domain.member.mypage.model.entity.Member;
import com.booktalk_be.domain.member.mypage.model.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member getMemberById (int id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));
    }
    
    //일단 테스트로 해보고 중복체크도 할 예정
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }
}
