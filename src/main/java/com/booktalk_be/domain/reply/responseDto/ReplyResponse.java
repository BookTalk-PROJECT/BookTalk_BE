package com.booktalk_be.domain.reply.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ReplyResponse {

    private final String replyCode;
    private final String memberId;
    private final String postCode;
    private final String content;
    private final LocalDate regDate;
    private final LocalDate updateDate;
    private final Integer likesCnt;
    private final Boolean isLike;

    private final List<ReplyResponse> replies;
}
