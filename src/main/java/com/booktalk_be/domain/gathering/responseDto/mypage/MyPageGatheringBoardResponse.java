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
public class MyPageGatheringBoardResponse {

    @JsonProperty("board_code")
    private String boardCode;

    @JsonProperty("gathering_name")
    private String gatheringName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("del_yn")
    private Integer delYn; // 0/1

    @JsonProperty("reg_date")
    private String regDate; // yyyy-MM-dd
}