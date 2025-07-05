package com.booktalk_be.domain.gathering.model.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class GatheringRecruitQuestionId implements Serializable {

    private Gathering code;

    private RecruitQuestion recruitQuestion;

}
