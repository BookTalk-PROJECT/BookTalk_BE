package com.booktalk_be.springconfig.auth.jwt;


import com.booktalk_be.domain.auth.model.entity.AuthorityType;
import com.booktalk_be.domain.auth.model.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
/* jwt 토큰 생성 및 검증, 관리 담당 클래스 */
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtProperties.getSecretAccessKey().getBytes());

    }

    public String getUsernameFromToken(final String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Integer getUserIdFromToken(final String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return Integer.valueOf(claims.get("userKey").toString());
    }

    public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateAccessToken(final String id, final int userKey, final AuthorityType userRole){
        return generateAccessToken(id, userKey, userRole, new HashMap<>());
    }

    public String generateAccessToken(final String id, final int userKey, final AuthorityType userRole, final Map<String, Object> claims) {
        claims.put("roles", userRole);
        claims.put("userKey", userKey);
        return doGenerateAccessToken(id, claims);
    }

    private String doGenerateAccessToken(final String id, final Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration())) // 30분
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(final String id, int userKey) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userKey", userKey);

        return doGenerateRefreshToken(id, claims);
    }

    private String doGenerateRefreshToken(final String id, final Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(id)
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration())) // 24시간
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key)
                .compact();
    }

    public void validateToken(final String token) throws JwtException {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 기존 메서드 (검증용 - 필터 등에서 사용)
    public Integer getUserKeyFromToken(String token) {
        Claims claims = getClaimsFromToken(token); // 여기서 만료되면 에러 터짐
        return (Integer) claims.get("userKey");
    }

    // (로그아웃용 - 만료 무시)
    public Integer getUserKeyFromExpiredToken(String token) {
        try {
            return getUserKeyFromToken(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return (Integer) e.getClaims().get("userKey");
        } catch (Exception e) {
            return null;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
