package com.booktalk_be.domain.likes.model.entity;

import com.booktalk_be.domain.auth.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class LikesId implements Serializable {

    private String code;
    private Member member;

}