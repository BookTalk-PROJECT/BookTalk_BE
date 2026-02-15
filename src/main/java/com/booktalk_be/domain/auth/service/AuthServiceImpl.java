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
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

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
        String refreshToken;

        if(refreshTokenService.validateExistMember(userKey)) {
            refreshTokenService.deleteRefreshToken(userKey);
            refreshTokenRepository.flush();
        }
        Refresh_Token newRefreshToken = refreshTokenService.saveRefreshToken(userKey);
        refreshToken = newRefreshToken.getToken();

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);
        return tokenMap;
    }

    @Override
    @Transactional
    public Map<String, String> reissueToken(String refreshToken) {

        jwtProvider.validateToken(refreshToken);

        Integer userId = jwtProvider.getUserIdFromToken(refreshToken);

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));

        if(refreshTokenService.validateExistMember(userId)){
            refreshTokenService.deleteRefreshToken(userId);
            refreshTokenRepository.flush();
        }
        String newRefreshToken = refreshTokenService.saveRefreshToken(userId).getToken();

        String newAccessToken = jwtProvider.generateAccessToken(
                member.getEmail(),
                member.getMemberId(),
                member.getAuthority()
        );

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", newAccessToken);
        tokenMap.put("refreshToken", newRefreshToken);
        return tokenMap;
    }
}
