package com.booktalk_be.springconfig.auditing;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == "anonymousUser"){
//            return null;
//        }
//
//        return Optional.of(((UserDetail) authentication.getPrincipal()).getUsername());
        return Optional.of("anonymousUser");
    }
}