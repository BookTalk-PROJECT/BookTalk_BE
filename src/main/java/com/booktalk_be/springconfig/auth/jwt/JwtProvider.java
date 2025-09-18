package com.booktalk_be.springconfig.auth.jwt;


import com.booktalk_be.domain.auth.model.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    private static final long JWT_EXPIRATION_TIME = 1000 * 60 * 30;

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtProperties.getSecretAccessKey().getBytes());
    }

    public String getUsernameFromToken(final String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    public Integer getUserIdFromToken(final String token) {
        return getClaimFromToken(token, claims -> claims.get("userKey", Integer.class));
    }

    public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {

        if(Boolean.FALSE.equals(validateToken(token)))
            return null;

        final Claims claims = getAllClaimsFromToken(token);

        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpirationDateFromToken(final String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String generateAccessToken(final String id, final int userKey){
        return generateAccessToken(id,userKey,new HashMap<>());
    }

//    public String generateAccessToken(final long id) {
//        return generateAccessToken(String.valueOf(id), new HashMap<>());
//    }

    public String generateAccessToken(final String id, final int userKey, final Map<String, Object> claims) {
        return doGenerateAccessToken(id, userKey, claims);
    }

    private String doGenerateAccessToken(final String id, final int userKey, final Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .claim("userKey", userKey)
                .setId(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME)) // 30분
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(final String id) {
        return doGenerateRefreshToken(id);
    }

    public String generateRefreshToken(final long id) {
        return doGenerateRefreshToken(String.valueOf(id));
    }

    private String doGenerateRefreshToken(final String id) {
        return Jwts.builder()
                .setId(id)
                .setExpiration(new Date(System.currentTimeMillis() + (JWT_EXPIRATION_TIME * 2) * 24)) // 24시간
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

}
