package com.booktalk_be.domain.gathering.responseDto.mypage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageRecruitApprovalResponse {

    @JsonProperty("gathering_code")
    private String gatheringCode;

    @JsonProperty("gathering_name")
    private String gatheringName;

    @JsonProperty("applicant_id")
    private Integer applicantId;

    @JsonProperty("applicant_name")
    private String applicantName;

    @JsonProperty("qa_json")
    private String qaJson;

    @JsonProperty("status")
    private String status; // WAITING | REJECT

    @JsonProperty("reject_reason")
    private String rejectReason;
}