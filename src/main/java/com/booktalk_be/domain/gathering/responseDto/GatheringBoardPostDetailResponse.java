package com.booktalk_be.domain.gathering.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringBoardPostDetailResponse {

    @JsonProperty("board_code")
    private String boardCode;

    @JsonProperty("member_id")
    private Integer memberId;

    private String gatheringCode;
    private String title;
    private String content;
    private String author;
    private String date;
    private Integer views;
    private Integer likesCnt;
    private Boolean notificationYn;
    private Boolean delYn;
    private String deleteReason;
}