package com.booktalk_be.domain.auth.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.auth.command.LoginDTO;
import com.booktalk_be.domain.auth.service.AuthService;
import com.booktalk_be.domain.auth.service.RefreshTokenService;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService; // 로그아웃 시 필요

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginData, HttpServletResponse response) {

        System.out.println("야 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            Map<String, String> tokens = authService.login(loginData);

            String accessToken  = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            ResponseCookie rtCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true) // HTTPS 환경에서만 쿠키 전송
                    .sameSite("Lax")
                    .path("/") // 쿠키 사용 경로 지정
                    .maxAge(Duration.ofDays(1))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

            return ResponseEntity.ok(ResponseDto.builder()
                    .code(200)
                    .data(Collections.singletonMap("accessToken", accessToken))
                    .build());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDto.builder()
                            .code(401)
                            .data(Collections.singletonMap("error", "아이디 또는 비밀번호가 틀렸습니다."))
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.builder()
                            .code(500)
                            .data(Collections.singletonMap("error", "인증 중 알 수 없는 오류 발생: " + e.getMessage()))
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletResponse response) {
        if (authentication != null && authentication.getPrincipal() instanceof Member) {
            Member member = (Member) authentication.getPrincipal();
            refreshTokenService.deleteRefreshToken(member.getMemberId());
        }

        ResponseCookie rtCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(Collections.singletonMap("message", "로그아웃 되었습니다."))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(name = "refresh_token") String refreshToken
    , HttpServletResponse response) {

            try {
                Map<String, String> tokens = authService.reissueToken(refreshToken);

                String acToken = tokens.get("accessToken");
                String rfToken = tokens.get("refreshToken");

                ResponseCookie rtCookie = ResponseCookie.from("refresh_token", rfToken)
                        .httpOnly(true)
                        .secure(true) // HTTPS 환경에서만 쿠키 전송
                        .sameSite("Lax")
                        .path("/") // 쿠키 사용 경로 지정
                        .maxAge(Duration.ofDays(1))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

                return ResponseEntity.ok(ResponseDto.builder()
                        .code(200)
                        .data(Collections.singletonMap("accessToken", acToken))
                        .build());
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseDto.builder()
                                .code(401)
                                .data(Collections.singletonMap("error", "아이디 또는 비밀번호가 틀렸습니다."))
                                .build());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseDto.builder()
                                .code(500)
                                .data(Collections.singletonMap("error", "인증 중 알 수 없는 오류 발생: " + e.getMessage()))
                                .build());
            }
    }
}
