package com.booktalk_be.domain.reply.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.common.baseEntity.CommonEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "reply")
public class Reply extends CommonEntity {
    //PK: REP_(prefix) + number
    @PrePersist
    public void generateId() {
        if(this.replyCode == null) {
            this.replyCode = "REP_" + System.currentTimeMillis();
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
    }

    @Id
    @Column(name = "reply_code", nullable = false)
    private String replyCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "post_code", nullable = false)
    private String postCode;

    @ManyToOne
    @JoinColumn(name = "parent_reply_code", nullable = true)
    private Reply parentReplyCode;

    @Column(name = "content", nullable = false)
    private String content;

    @ColumnDefault("0")
    @Column(name = "like_cnt", nullable = false)
    @Formula("(SELECT count(1) FROM likes l WHERE l.code = reply_code)")
    protected Integer likesCnt;

    @Column(name = "del_yn", nullable = false)
    private Boolean delYn;

    @Column(name = "del_reason", nullable = true)
    private String delReason;

    @Builder
    public Reply(Member member, String postCode, Reply parentReplyCode, String content) {
        this.member = member;
        this.postCode = postCode;
        this.parentReplyCode = parentReplyCode;
        this.content = content;
    }
}
