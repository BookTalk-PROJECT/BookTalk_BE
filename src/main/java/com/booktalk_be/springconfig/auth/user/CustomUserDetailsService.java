package com.booktalk_be.springconfig.auth.user;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import com.booktalk_be.domain.member.mypage.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다 : "+username));
        return new CustomUserDetails(member);
    }
}
