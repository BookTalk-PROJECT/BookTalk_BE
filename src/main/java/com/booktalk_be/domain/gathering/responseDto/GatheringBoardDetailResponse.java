package com.booktalk_be.domain.gathering.responseDto;


import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GatheringBoardDetailResponse {
    private final GatheringBoardPostDetailResponse post;
    private final List<ReplyResponse> replies;
}