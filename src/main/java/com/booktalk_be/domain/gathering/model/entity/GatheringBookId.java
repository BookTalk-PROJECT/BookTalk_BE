package com.booktalk_be.domain.gathering.model.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@NoArgsConstructor // 추가! 꼭 public이어야 함
@Getter
@EqualsAndHashCode
public class GatheringBookId implements Serializable {

    private Gathering code;
    private String isbn;
}
