// src/main/java/com/booktalk_be/domain/gathering/model/entity/RecruitRequest.java
package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(RecruitRequestId.class)
@Table(name = "recruit_request")
public class RecruitRequest {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_code", nullable = false)
    private Gathering code;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Id
    @Column(name = "recruit_question", nullable = false, length = 100)
    private String recruitQuestion;

    @Column(name = "request_question_answer")
    private String requestQuestionAnswer; // JSON 문자열로 저장

    @Convert(converter = RecruitRequestStatus.Converter.class) //  컨버터 추가
    @Column(name = "status", nullable = false)
    private RecruitRequestStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Builder
    public RecruitRequest(Gathering code,
                          Member member,
                          String recruitQuestion,
                          String requestQuestionAnswer,
                          RecruitRequestStatus status,
                          String rejectReason) {
        this.code = code;
        this.member = member;
        this.recruitQuestion = recruitQuestion;
        this.requestQuestionAnswer = requestQuestionAnswer;
        this.status = status;
        this.rejectReason = rejectReason;
    }
}
