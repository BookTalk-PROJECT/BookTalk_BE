package com.booktalk_be.domain.auth.service;

import com.booktalk_be.domain.auth.command.LoginDTO;
import com.booktalk_be.domain.auth.model.entity.AuthorityType;
import com.booktalk_be.domain.auth.model.entity.Refresh_Token;
import com.booktalk_be.domain.auth.model.repository.RefreshTokenRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import com.booktalk_be.springconfig.auth.jwt.JwtProvider;
import com.booktalk_be.springconfig.auth.user.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Map<String, String> login(LoginDTO loginData) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginData.getUsername(), loginData.getPassword()
                )
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        int userKey = userDetails.getMember().getMemberId();
        AuthorityType userRole = userDetails.getMember().getAuthority();
        String accessToken = jwtProvider.generateAccessToken(userId, userKey, userRole);
        String refreshToken = "";

        Member member = memberRepository.findById(userDetails.getMember().getMemberId())
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다"));

        if(!refreshTokenRepository.existsByMember(member)) {
            refreshToken = jwtProvider.generateRefreshToken(userId);
            Refresh_Token token_entity = Refresh_Token.builder()
                    .user(member)
                    .refreshToken(refreshToken).build();

            refreshTokenRepository.save(token_entity);
        }else{
            Refresh_Token refresh_token= refreshTokenRepository.findByMember(member);
            refreshToken = refresh_token.getToken();
        }

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);
        return tokenMap;
    }
}
