package com.booktalk_be.springconfig.auth.user;

import com.booktalk_be.domain.member.model.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    @Getter
    private final Member member;

    private final Map<String, Object> attributes;

    public CustomOAuth2User(Map<String, Object> attributes, Member member) {
        this.attributes = attributes;
        this.member = member;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getAuthority()));
    }

    @Override
    public String getName() {
        return member.getName();
    }

}
