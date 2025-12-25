package com.booktalk_be.springconfig.auth.jwt;

import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.service.MemberService;
import com.booktalk_be.springconfig.exception.Dto.ErrorDto;
import com.booktalk_be.springconfig.exception.utils.ErrorResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    private static final List<String> EXCLUDED_URLS = List.of(
            "/login","/refresh", "/logout"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 허용된 URL이면 토큰 검증 스킵
        if (EXCLUDED_URLS.stream().anyMatch(request.getRequestURI()::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        try {
            if (StringUtils.hasText(token)) {
                jwtProvider.validateToken(token); // 토큰 유효성 검사 (예외 발생 가능)

                Claims claims = jwtProvider.getClaimsFromToken(token);
                Integer userId = (Integer) claims.get("userKey");

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(getUserAuth(userId));
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            ErrorDto dto = ErrorResponseUtil.fromJwtException(e, false); // access
            ErrorResponseUtil.write(response, dto);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private UsernamePasswordAuthenticationToken getUserAuth(int userId) {
        Member memberInfo = memberService.getMemberById(userId);
        return new UsernamePasswordAuthenticationToken(memberInfo,
                null,
                Collections.singleton(new SimpleGrantedAuthority(memberInfo.getAuthority().toString())));
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String errorCode, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorCode", errorCode); // 추가된 에러 코드
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(body));
    }
}
