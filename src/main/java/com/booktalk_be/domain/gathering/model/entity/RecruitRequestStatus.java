package com.booktalk_be.domain.gathering.model.entity;


import com.booktalk_be.common.utils.EntityEnumerable;
import com.booktalk_be.common.utils.EntityEnumerableConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RecruitRequestStatus implements EntityEnumerable {
    WAITING("WAITING", "대기"),
    REJECT("REJECT", "거부");

    private final String type;
    private final String name;

    @jakarta.persistence.Converter
    public static class Converter extends EntityEnumerableConverter<RecruitRequestStatus> {
        public Converter() {
            super(com.booktalk_be.domain.gathering.model.entity.RecruitRequestStatus.class);
        }
    }
}
