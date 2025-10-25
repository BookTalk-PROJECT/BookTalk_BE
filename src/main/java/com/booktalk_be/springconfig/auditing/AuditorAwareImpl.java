package com.booktalk_be.springconfig.auditing;

import com.booktalk_be.domain.member.model.entity.Member;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser"){
            return null;
        }

        return Optional.of(((Member) authentication.getPrincipal()).getEmail());
    }
}