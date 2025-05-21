package com.booktalk_be.domain.board.model.entity;

import com.booktalk_be.domain.member.auth.model.entity.Member;
import com.booktalk_be.common.baseEntity.Post;
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
    private Long categoryId;

    @Builder
    public Board(Member member, Long categoryId, String title, String content, Boolean delYn, Boolean notificationYn) {
        this.member = member;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.notificationYn = notificationYn;
    }
}
