package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.common.entity.Post;
import com.booktalk_be.domain.gathering.command.UpdateGatheringBoardCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "gathering_board")
public class GatheringBoard extends Post {

    @PrePersist
    public void generateId() {
        if (this.code == null) this.code = "GAB_" + System.currentTimeMillis();
        if (this.delYn == null) this.delYn = false;
        if (this.views == null) this.views = 0;
        if (this.notificationYn == null) this.notificationYn = false;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gathering_code")
    private Gathering gathering;

    @Builder
    public GatheringBoard(Gathering gathering, Member member, String title, String content, Boolean notificationYn) {
        this.gathering = gathering;
        this.member = member;                 // Post.member
        this.title = title;
        this.content = content;
        this.notificationYn = (notificationYn != null ? notificationYn : false);
        this.views = 0;
        this.delYn = false;
    }

    public void modify(UpdateGatheringBoardCommand cmd) {
        if (cmd.getTitle() != null) this.title = cmd.getTitle();
        if (cmd.getContent() != null) this.content = cmd.getContent();
        if (cmd.getNotification_yn() != null) this.notificationYn = cmd.getNotification_yn();
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
}