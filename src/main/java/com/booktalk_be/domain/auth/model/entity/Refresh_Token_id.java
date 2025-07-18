package com.booktalk_be.domain.auth.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Refresh_Token_id implements Serializable {

    private Member member;

    private String token;
}
