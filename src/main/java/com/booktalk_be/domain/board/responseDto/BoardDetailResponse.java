package com.booktalk_be.domain.board.responseDto;

import com.booktalk_be.common.responseDto.PostDetailResponse;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardDetailResponse {
    private final CommuDetailResponse post;
    private final List<ReplyResponse> replies;
}