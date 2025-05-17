package com.booktalk_be.auth.model.entity;

import com.booktalk_be.common.utils.EntityEnumerable;
import com.booktalk_be.common.utils.EntityEnumerableConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AuthorityType implements EntityEnumerable {
    ADMIN("ADMIN", "관리자"),
    COMMON("COMMON", "일반 사용자");

    private final String type;
    private final String name;

    @jakarta.persistence.Converter
    public static class Converter extends EntityEnumerableConverter<AuthorityType> {
        public Converter() {
            super(AuthorityType.class);
        }
    }
}
