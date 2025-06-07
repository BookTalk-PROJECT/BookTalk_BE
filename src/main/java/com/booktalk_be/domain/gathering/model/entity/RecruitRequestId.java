package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RecruitRequestId implements Serializable {

    private Gathering code;
    private Member member;

}


