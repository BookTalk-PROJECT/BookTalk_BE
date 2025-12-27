package com.booktalk_be.domain.gathering.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatheringBoardResponse {

    @JsonProperty("board_code")
    private String boardCode;

    private String title;
    private Boolean delYn;
    private String deleteReason;
    private String author;
    private String date;
    private Integer views;
}