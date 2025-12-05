package com.booktalk_be.domain.gathering.model.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruit_question") // 참여신청 질문 모음 테이블
public class RecruitQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruit_question;

    @Column(name = "question_order", nullable = false)
    private Integer order;

    @Column(name = "question", nullable = false)
    private String question;

    @Builder
    public RecruitQuestion(Integer order, String question) {
        this.order = order;
        this.question = question;
    }

    public void update(Integer order, String question) {
        if (order != null) {
            this.order = order;
        }
        if (question != null) {
            this.question = question;
        }
    }
}
