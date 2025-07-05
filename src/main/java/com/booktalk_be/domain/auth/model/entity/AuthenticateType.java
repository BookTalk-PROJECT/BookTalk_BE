package com.booktalk_be.domain.auth.model.entity;

import com.booktalk_be.common.utils.EntityEnumerable;
import com.booktalk_be.common.utils.EntityEnumerableConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AuthenticateType implements EntityEnumerable {
    OWN("OWN", "자체 유저"),
    KAKAO("KAKAO", "카카오 소셜 유저"),
    NAVER("NAVER", "네이버 소셜 유저");

    private final String type;
    private final String name;

    @jakarta.persistence.Converter
    public static class Converter extends EntityEnumerableConverter<AuthenticateType> {
        public Converter() {
            super(AuthenticateType.class);
        }
    }
}
