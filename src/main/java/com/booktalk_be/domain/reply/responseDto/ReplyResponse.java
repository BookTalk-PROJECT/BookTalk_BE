package com.booktalk_be.domain.reply.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ReplyResponse {
    @JsonProperty("reply_code")
    private String replyCode;
    @JsonProperty("member_name")
    private String memberName;
    private String postCode;
    private String content;
    @JsonProperty("create_at")
    private String regDate;
    private String updateDate;
    @JsonProperty("likes")
    private Integer likesCnt;
    @JsonProperty("is_liked")
    private Boolean isLiked;

    private List<ReplyResponse> replies;
}
