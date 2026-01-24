package com.booktalk_be.domain.gathering.command.mypage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringReplySearchCondCommand {

    @JsonProperty("keywordType")
    private String keywordType; // gathering_name | post_title | content | author

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;
}