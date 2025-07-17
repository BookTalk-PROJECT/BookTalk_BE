package com.booktalk_be.domain.likes.model.entity;

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
public class LikesId implements Serializable {

    private String code;
    private Member member;

}