package com.booktalk_be.domain.gathering.responseDto.mypage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageGatheringResponse {

    @JsonProperty("gathering_code")
    private String gatheringCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("leader_name")
    private String leaderName;

    @JsonProperty("master_yn")
    private Integer masterYn;

    @JsonProperty("del_yn")
    private Boolean delYn;

    @JsonProperty("reg_date")
    private String regDate; // yyyy-MM-dd
}