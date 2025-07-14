package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.common.baseEntity.Post;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gathering_board") // 모임 게시글 엔티티
public class GatheringBoard extends Post {
    //PK: GAB_(prefix) + number
    @PrePersist
    public void generateId(){
        if(this.code == null){
            this.code = "GAB_"+System.currentTimeMillis();
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gathering_board_code")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;
}
