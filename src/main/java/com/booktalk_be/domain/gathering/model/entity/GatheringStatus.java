package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.common.utils.EntityEnumerable;
import com.booktalk_be.common.utils.EntityEnumerableConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum GatheringStatus implements EntityEnumerable{
    INTENDED("INTENDED", "모집중"),
    PROGRESS("PROGRESS", "진행중"),
    END("END", "완료");


    private final String type;
    private final String name;

    @jakarta.persistence.Converter
    public static class Converter extends EntityEnumerableConverter<com.booktalk_be.domain.gathering.model.entity.GatheringStatus> {
        public Converter() {
            super(com.booktalk_be.domain.gathering.model.entity.GatheringStatus.class);
        }
    }
}
