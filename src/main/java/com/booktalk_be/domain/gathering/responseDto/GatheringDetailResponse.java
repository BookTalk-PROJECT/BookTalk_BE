package com.booktalk_be.domain.gathering.responseDto;


import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GatheringDetailResponse {

    private String code;                 // PK
    private String name;                 // 모임 이름
    private GatheringStatus status;      // 상태 (문자열 저장 권장)
    private Long recruitmentPersonnel;   // 모집 인원수
    private String recruitmentPeriod;    // 모집 기간
    private String activityPeriod;       // 활동 기간
    private String emdCd;                // 읍면동 코드
    private String sigCd;                // 행정구역 코드
    private String imageUrl;             // 이미지 경로
    private String summary;              // 모임소개

    public static GatheringDetailResponse from(Gathering g) {
        return GatheringDetailResponse.builder()
                .code(g.getCode())
                .name(g.getName())
                .status(g.getStatus())
                .recruitmentPersonnel(g.getRecruitmentPersonnel())
                .recruitmentPeriod(g.getRecruitmentPeriod())
                .activityPeriod(g.getActivityPeriod())
                .emdCd(g.getEmdCd())
                .sigCd(g.getSigCd())
                .imageUrl(g.getImageUrl())
                .summary(g.getSummary())
                .build();
    }
}
