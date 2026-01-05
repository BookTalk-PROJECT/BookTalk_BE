package com.booktalk_be.domain.board.responseDto;

import com.booktalk_be.common.responseDto.PostDetailResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CommuDetailResponse extends PostDetailResponse {
    @JsonProperty("category_id")
    private String categoryId;
}
