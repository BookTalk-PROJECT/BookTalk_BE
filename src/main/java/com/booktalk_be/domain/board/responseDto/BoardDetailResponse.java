package com.booktalk_be.domain.board.responseDto;

import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
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

    private final String boardCode;
    private final String memberId;
    private final String title;
    private final String content;
    private final Integer views;
    private final Integer likesCnt;
    private final LocalDate regDate;
    private final LocalDateTime updateDate;
    private final Boolean isLike;
    private final Boolean notificationYn;

    private final List<ReplyResponse> replies;

}
