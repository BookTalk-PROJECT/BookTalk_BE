package com.booktalk_be.springconfig.auth.jwt;

import io.jsonwebtoken.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    //private final UserGetService userGetService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String token = request.getHeader("Authorization");

        String username = null;

        if(token != null && !token.isEmpty()) {
            String jwtToken = token.substring(7);

            username = jwtProvider.getUsernameFromToken(jwtToken);
        }

        if(username != null && !username.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            //SecurityContextHolder.getContext().setAuthentication(getUserAuth(username));
        }

        filterChain.doFilter(request, response);
    }

    //private UsernamePasswordAuthenticationToken getUserAuth(String username) {
        //var userInfo = userGetService.getUserById(Long.parseLong(username));

        //return new UsernamePasswordAuthenticationToken(userInfo.id(),
         //       userInfo.password(),
         //       Collections.singleton(new SimpleGrantedAuthority(userInfo.roleName().name)));
    //}
}
