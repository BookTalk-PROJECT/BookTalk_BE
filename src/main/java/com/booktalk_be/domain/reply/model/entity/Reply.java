package com.booktalk_be.domain.reply.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.common.entity.CommonEntity;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "reply")
public class Reply extends CommonEntity {

    @Transient
    private static com.booktalk_be.common.utils.DistributedIdGenerator idGenerator;

    public static void setIdGenerator(com.booktalk_be.common.utils.DistributedIdGenerator generator) {
        idGenerator = generator;
    }

    //PK: REP_(prefix) + number
    @PrePersist
    public void generateId() {
        if(this.replyCode == null) {
            if (idGenerator != null) {
                this.replyCode = idGenerator.generateReplyId();
            } else {
                this.replyCode = "REP_" + System.currentTimeMillis();
            }
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
        if(this.likesCnt == null) {
            this.likesCnt = 0;
        }
    }

    @Id
    @Column(name = "reply_code", nullable = false)
    private String replyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "post_code", nullable = false)
    private String postCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_code", nullable = true)
    private Reply parentReplyCode;

    @Column(name = "content", nullable = false)
    private String content;

    @ColumnDefault("0")
    @Column(name = "like_cnt", nullable = false)
    protected Integer likesCnt = 0;

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

    public void modify(UpdateReplyCommand cmd) {
        this.content = cmd.getContent();
    }

    public void delete() {
        this.delYn = true;
    }

    public void delete(String reason) {
        this.delYn = true;
        this.delReason = reason;
    }

    public void recover() {
        this.delYn = false;
        this.delReason = null;
    }

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
