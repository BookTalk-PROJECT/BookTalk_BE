package com.booktalk_be.springconfig.auth;

import com.booktalk_be.springconfig.auth.jwt.JwtAuthFilter;
import com.booktalk_be.springconfig.auth.user.CustomAccessDeniedHandler;
import com.booktalk_be.springconfig.auth.user.CustomAuthenticationEntryPointHandler;
import com.booktalk_be.springconfig.auth.user.CustomOAuth2UserService;
import com.booktalk_be.springconfig.auth.user.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig  {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 카테고리 관리 (admin 전용) — community/** 보다 먼저 선언해야 우선 적용
                        .requestMatchers("/community/category/create").hasAuthority("ADMIN")
                        .requestMatchers("/community/category/modify").hasAuthority("ADMIN")
                        .requestMatchers("/community/category/delete/**").hasAuthority("ADMIN")
                        .requestMatchers("/community/category/reorder").hasAuthority("ADMIN")
                        .requestMatchers("/community/category/admin/**").hasAuthority("ADMIN")
                        // 게시글 관리
                        .requestMatchers("/community/board/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/community/board/restrict").hasAuthority("ADMIN")
                        .requestMatchers("/community/board/recover/**").hasAuthority("ADMIN")
                        // 댓글 관리
                        .requestMatchers("/reply/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/reply/restrict").hasAuthority("ADMIN")
                        .requestMatchers("/reply/recover/**").hasAuthority("ADMIN")
                        // 북리뷰 관리
                        .requestMatchers("/book-reviews/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/book-reviews/restrict").hasAuthority("ADMIN")
                        .requestMatchers("/book-reviews/recover/**").hasAuthority("ADMIN")
                        // 회원 목록·권한 관리
                        .requestMatchers("/member/list").hasAuthority("ADMIN")
                        .requestMatchers("/member/list/search").hasAuthority("ADMIN")
                        .requestMatchers("/member/role/**").hasAuthority("ADMIN")
                        // 기존 public 경로
                        .requestMatchers("/uploads/**","/login","/refresh","/gathering/**", "/community/**", "/reply/**", "/member/**", "/dashboard/**", "/token-refresh", "/error","/nlk/**","/oauth/**","/logout","/book-reviews/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint(new CustomAuthenticationEntryPointHandler())
                        .accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
