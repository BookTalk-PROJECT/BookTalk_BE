package com.booktalk_be.domain.gathering.model.entity;


import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(RecruitRequestId.class)
@Table(name = "recruit_request") // 참여신청답변 테이블
public class RecruitRequest {

    @Id
    @ManyToOne
    @JoinColumn(name = "gathering_code", nullable = false)
    private Gathering code;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "request_question_answer")
    private String requestQuestionAnswer;

    @Column(name = "status")
    private RecruitRequestStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;
}
