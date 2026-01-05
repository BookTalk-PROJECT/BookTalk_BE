package com.booktalk_be.domain.bookreview.dto;

import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BookReviewDetailResponse {
    private final BookReviewDetailDto post;
    private final List<ReplyResponse> replies;
}
