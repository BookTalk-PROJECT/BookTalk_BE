package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class GatheringMemberId implements Serializable {

    private Gathering code;

    private Member member;
}
