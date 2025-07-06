package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class GatheringMemberId implements Serializable {

    private Gathering code;

    private Member member;
}
