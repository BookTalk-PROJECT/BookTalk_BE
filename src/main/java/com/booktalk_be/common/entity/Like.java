package com.booktalk_be.common.entity;

import com.booktalk_be.auth.model.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@IdClass(LikeId.class)
@Table(name = "likes")
public class Like extends BaseTimeEntity {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

}
