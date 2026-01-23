package com.booktalk_be.domain.gathering.command.mypage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringSearchCondCommand {

    /**
     * 예: "gathering_code", "name"
     * (프론트 rowDef.key 그대로 내려오게)
     */
    @JsonProperty("keywordType")
    private String keywordType;

    @JsonProperty("keyword")
    @Size(max = 100)
    private String keyword;

    /**
     * "YYYY-MM-DD" 문자열로 받고 DB 프로시저에서 DATE로 처리
     * 비어있으면 조건 제외
     */
    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;
}