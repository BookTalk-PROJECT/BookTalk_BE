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
//GatheringRecruitQuestionId에 있는 복합키 클래스를 불러와서 사용?
@Table(name = "gathering_recruit_question_map")
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
