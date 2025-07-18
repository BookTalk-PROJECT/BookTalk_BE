package com.booktalk_be.common.baseEntity;

import com.booktalk_be.domain.member.mypage.model.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;

@Getter
@MappedSuperclass
public abstract class Post extends CommonEntity {
    @Id
    @Column(name = "code", nullable = false)
    protected String code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    protected Member member;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "content", nullable = false)
    protected String content;

    @ColumnDefault("0")
    @Column(name = "views", nullable = false)
    protected Integer views;

    @ColumnDefault("0")
    @Column(name = "like_cnt", nullable = false)
    @Formula("(SELECT count(1) FROM likes l WHERE l.code = code)")
    protected Integer likesCnt;

    @Column(name = "del_yn", nullable = false)
    protected Boolean delYn;

    @Column(name = "notification_yn", nullable = false)
    protected Boolean notificationYn;

    @Column(name = "del_reason", nullable = true)
    protected String delReason;
}