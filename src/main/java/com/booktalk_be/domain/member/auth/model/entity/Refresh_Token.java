package com.booktalk_be.domain.member.auth.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "refresh_token")
@Getter
public class Refresh_Token {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "refresh_token", nullable = false)
    private String token;

}
