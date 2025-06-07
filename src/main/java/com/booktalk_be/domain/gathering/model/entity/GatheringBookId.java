package com.booktalk_be.domain.gathering.model.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class GatheringBookId {

    private Gathering code;
    private String isbn;

}
