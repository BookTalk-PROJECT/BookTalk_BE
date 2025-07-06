package com.booktalk_be.domain.auth.controller;

import com.booktalk_be.domain.auth.command.LoginDTO;
import com.booktalk_be.domain.auth.service.LoginService;
import com.booktalk_be.springconfig.auth.jwt.JwtProvider;
import com.booktalk_be.springconfig.auth.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.juli.logging.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginData) {
        try {
            Map<String, String> tokens = loginService.login(loginData);
            return ResponseEntity.ok(tokens);
        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials: {}" + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "아이디 또는 비밀번호가 틀렸습니다."));
        } catch (Exception e) {
            System.out.println("인증 중 알 수 없는 오류 발생" + e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "인증 실패: " + e.getMessage()));
        }

    }
}


