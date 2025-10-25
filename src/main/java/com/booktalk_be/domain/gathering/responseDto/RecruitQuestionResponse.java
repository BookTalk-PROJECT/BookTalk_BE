package com.booktalk_be.domain.gathering.responseDto;

import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionMap;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class RecruitQuestionResponse {
    private Long id;        // recruit_question PK
    private String question;
    private Integer order;  // 표시 순서

    // 프론트 호환을 위한 기본값 (DB에 없으므로 임시)
    private boolean required;   // 기본 false
    private int maxLength;      // 기본 500

    public static RecruitQuestionResponse from(GatheringRecruitQuestionMap gm) {
        var rq = gm.getRecruitQuestion();
        return RecruitQuestionResponse.builder()
                .id(rq.getRecruit_question())
                .question(rq.getQuestion())
                .order(rq.getOrder())
                .required(false)  // TODO: 컬럼 생기면 매핑 변경
                .maxLength(500)   // TODO: 컬럼 생기면 매핑 변경
                .build();
    }
}
