package com.booktalk_be.domain.auth.model.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secretAccessKey;
    private String secretRefreshToken;
    private int accessExpiration;
    private int refreshExpiration;
}
