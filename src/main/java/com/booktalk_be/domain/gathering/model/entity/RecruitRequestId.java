package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RecruitRequestId implements Serializable {

    private Gathering code;
    private Member member;

}


