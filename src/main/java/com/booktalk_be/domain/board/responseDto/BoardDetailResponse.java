package com.booktalk_be.domain.board.responseDto;

import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardDetailResponse {
    private final CommuDetailResponse post;
    private final List<ReplyResponse> replies;
}