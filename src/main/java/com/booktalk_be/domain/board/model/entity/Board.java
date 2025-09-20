package com.booktalk_be.domain.board.model.entity;

import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.common.baseEntity.Post;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "board")
public class Board extends Post {
    //PK: BO_(prefix) + number
    @PrePersist
    public void generateId() {
        if(this.code == null) {
            this.code = "BO_" + System.currentTimeMillis();
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
    }

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Builder
    public Board(Member member, Integer categoryId, String title, String content, Boolean delYn, Boolean notificationYn) {
        this.member = member;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.notificationYn = notificationYn;
    }

    public void modify(UpdateBoardCommand cmd) {
        this.title = cmd.getTitle();
        this.content = cmd.getContent();
        this.notificationYn = cmd.getNotification_yn();
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
