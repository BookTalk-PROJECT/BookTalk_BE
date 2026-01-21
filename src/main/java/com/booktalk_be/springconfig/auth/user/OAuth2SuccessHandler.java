package com.booktalk_be.springconfig.auth.user;

import com.booktalk_be.domain.auth.model.entity.Refresh_Token;
import com.booktalk_be.domain.auth.model.repository.RefreshTokenRepository;
import com.booktalk_be.domain.auth.service.RefreshTokenServiceImpl;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.service.MemberService;
import com.booktalk_be.springconfig.auth.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String redirectBaseURL = "http://localhost:5173/oauth/callback/" + oAuth2User.getMember().getAuthType().getType().toLowerCase();

        Member member = memberService.getMemberById(oAuth2User.getMember().getMemberId());
        int memberId = member.getMemberId();

        String accessToken = jwtProvider.generateAccessToken(member.getEmail(),member.getMemberId(),member.getAuthority());

        String refreshToken = "";

        if(refreshTokenService.validateExistMember(memberId)) {
            refreshTokenService.deleteRefreshToken(memberId);
            refreshTokenRepository.flush();
        }
        Refresh_Token newRefreshToken = refreshTokenService.saveRefreshToken(memberId);
        refreshToken = newRefreshToken.getToken();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .sameSite("Lax") //
                .maxAge(Duration.ofDays(7))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.sendRedirect(redirectBaseURL);
    }
}
