package com.booktalk_be.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class OauthController {

    @GetMapping("/kakao")
    public void kakaoLogin(HttpServletResponse response) throws Exception {
        String kakaoAuthUrl = "https://kakao.com";


        //302 redirect 보냄
        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/logincomplete")
    public ResponseEntity<?> Login(@AuthenticationPrincipal OAuth2User user) throws Exception {

        System.out.println(user.getAttributes());


        return ResponseEntity.ok(user);
    }

}
