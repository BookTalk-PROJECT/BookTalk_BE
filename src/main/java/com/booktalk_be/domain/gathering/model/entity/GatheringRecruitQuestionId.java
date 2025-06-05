package com.booktalk_be.domain.gathering.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class GatheringRecruitQuestionId implements Serializable {

    private Gathering code;

    private RecruitQuestion recruitQuestion;

}
