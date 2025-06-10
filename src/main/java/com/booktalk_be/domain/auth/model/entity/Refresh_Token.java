package com.booktalk_be.domain.auth.model.entity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "refresh_token")
@Getter
public class Refresh_Token {

    @Id
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "refresh_token", nullable = false)
    private String token;

}
