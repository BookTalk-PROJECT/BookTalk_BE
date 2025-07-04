package com.booktalk_be.domain.auth.model.entity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refresh_Token {

    @Id
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "refresh_token", nullable = false)
    private String token;

    @Builder
    public Refresh_Token(Member user, String refreshToken) {
        this.member = user;
        this.token = refreshToken;
    }
}
