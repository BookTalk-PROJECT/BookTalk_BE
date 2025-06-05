package com.booktalk_be.domain.gathering.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(GatheringRecruitQuestionId.class)
@Table(name = "gathering_recruit_question_map") // 모임_참여신청질문 매핑 엔티티
public class GatheringRecruitQuestionMap {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering code;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_question")
    private RecruitQuestion recruitQuestion;

}
