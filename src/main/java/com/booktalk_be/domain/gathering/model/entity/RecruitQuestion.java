package com.booktalk_be.domain.gathering.model.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//참여신청 질문 모음 테이블
@Table(name = "recruit_question")
public class RecruitQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruit_question;

    @Column(name = "value", nullable = false)
    private String value;


}
