package com.booktalk_be.domain.auth.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.auth.command.LoginDTO;
import com.booktalk_be.domain.auth.service.LoginService;
import com.booktalk_be.springconfig.auth.jwt.JwtProvider;
import com.booktalk_be.springconfig.auth.user.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.juli.logging.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginData, HttpServletResponse response) {
        try {
            Map<String, String> tokens = loginService.login(loginData);

            String accessToken  = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            // 1) Refresh Token을 HttpOnly 쿠키로 전달
            ResponseCookie rtCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
//                    .secure(true)
                    .sameSite("Lax")
                    .path("/auth/refresh")
                    .maxAge(Duration.ofDays(30))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

            return ResponseEntity.ok(ResponseDto.builder()
                    .code(200)
                    .data(Collections.singletonMap("accessToken", accessToken))
                    .build());
        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials: {}" + e.getMessage());
            return ResponseEntity.ok(ResponseDto.builder()
                    .code(200)
                    .data(Collections.singletonMap("error", "아이디 또는 비밀번호가 틀렸습니다."))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("인증 중 알 수 없는 오류 발생" + e);
            return ResponseEntity.ok(ResponseDto.builder()
                    .code(200)
                    .data(Collections.singletonMap("error", "인증 실패: " + e.getMessage()))
                    .build());
        }
    }
}


