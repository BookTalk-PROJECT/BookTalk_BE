package com.booktalk_be.domain.gathering.model.entity;


import com.booktalk_be.domain.likes.model.entity.LikesId;
import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@IdClass(RecruitRequestId.class)
@Table(name = "recruit_request")
//RecruitRequestId에 있는 복합키 클래스를 불러와서 사용?
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
