package com.booktalk_be.community.board.model.entity;

import com.booktalk_be.auth.model.entity.Member;
import com.booktalk_be.common.entity.Post;
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
        this.code = "BO_" + System.currentTimeMillis();
        this.delYn = false;
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
