package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.likes.model.entity.LikesId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
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
