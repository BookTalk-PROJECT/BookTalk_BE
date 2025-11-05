package com.booktalk_be.domain.auth.service;

import com.booktalk_be.domain.auth.model.entity.Refresh_Token;
import com.booktalk_be.domain.auth.model.repository.RefreshTokenRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import com.booktalk_be.springconfig.auth.jwt.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @Override
    public boolean validateExistMember(int memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        return refreshTokenRepository.existsByMember(member);
    }

    @Override
    public Refresh_Token saveRefreshToken(int memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        String refreshToken = null;
        if (member != null) {
            refreshToken = jwtProvider.generateRefreshToken(member.getEmail());
        }
        Refresh_Token token_entity = Refresh_Token.builder()
                .user(member)
                .refreshToken(refreshToken).build();
        return  refreshTokenRepository.save(token_entity);
    }

    @Override
    public Refresh_Token getRefreshTokenByMember(int memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        return refreshTokenRepository.findByMember(member);
    }
}
