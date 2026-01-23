package com.booktalk_be.domain.gathering.command.mypage;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringBoardSearchCondCommand {

    @JsonProperty("keywordType")
    private String keywordType; // gathering_name | title | author

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("startDate")
    private String startDate; // yyyy-MM-dd

    @JsonProperty("endDate")
    private String endDate; // yyyy-MM-dd
}