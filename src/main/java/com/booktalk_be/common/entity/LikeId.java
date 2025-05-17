package com.booktalk_be.common.entity;

import com.booktalk_be.auth.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class LikeId implements Serializable {

    private String code;
    private Member member;

}