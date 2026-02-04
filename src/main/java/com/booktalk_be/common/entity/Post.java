package com.booktalk_be.common.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;


@Getter
@MappedSuperclass
public abstract class Post extends CommonEntity {
    @Id
    @Column(name = "code", nullable = false)
    protected String code;

    @ManyToOne(fetch = FetchType.LAZY)
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
    protected Integer likesCnt = 0;

    @Column(name = "del_yn", nullable = false)
    protected Boolean delYn;

    @Column(name = "notification_yn", nullable = false)
    protected Boolean notificationYn;

    @Column(name = "del_reason", nullable = true)
    protected String delReason;

    public void incrementLikes() {
        if (this.likesCnt == null) {
            this.likesCnt = 0;
        }
        this.likesCnt++;
    }

    public void decrementLikes() {
        if (this.likesCnt != null && this.likesCnt > 0) {
            this.likesCnt--;
        }
    }
}