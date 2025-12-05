// src/main/java/com/booktalk_be/domain/gathering/responseDto/GatheringDetailResponse.java
package com.booktalk_be.domain.gathering.responseDto;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GatheringDetailResponse {

    private String gatheringCode;
    private String name;
    private int status;
    private Long recruitmentPersonnel;
    private String recruitmentPeriod;
    private String activityPeriod;
    private String emdCd;
    private String sigCd;
    private String summary;

    private boolean delYn;
    private String delReason;

    /** 모임 개설자 여부 (1: 개설자, 0: 일반/비회원) */
    private int masterYn;

    // 기존 from(Gathering) 대신 masterYn을 받는 팩토리 사용
    public static GatheringDetailResponse from(Gathering g, int masterYn) {
        return GatheringDetailResponse.builder()
                .gatheringCode(g.getCode())
                .name(g.getName())
                .status(toStatusCode(g.getStatus()))
                .recruitmentPersonnel(g.getRecruitmentPersonnel())
                .recruitmentPeriod(g.getRecruitmentPeriod())
                .activityPeriod(g.getActivityPeriod())
                .emdCd(g.getEmdCd())
                .sigCd(g.getSigCd())
                .summary(g.getSummary())
                .delYn(Boolean.TRUE.equals(g.getDelYn()))
                .delReason(g.getDelReason())
                .masterYn(masterYn)
                .build();
    }

    private static int toStatusCode(GatheringStatus status) {
        if (status == null) return 0;  // 기본값: 모집중

        return switch (status) {
            case INTENDED -> 0;
            case PROGRESS -> 1;
            case END      -> 2;
        };
    }
}
