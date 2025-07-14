package com.booktalk_be.domain.likes.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.common.baseEntity.CommonTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@IdClass(LikesId.class)
@Table(name = "likes")
public class Likes extends CommonTimeEntity {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

}
