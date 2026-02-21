package com.booktalk_be.common.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostDetailResponse {
    @JsonProperty("board_code")
    private String boardCode;
    @JsonProperty("member_id")
    private int memberId;
    private String title;
    private String content;
    private String author;
    private Integer views;
    @JsonProperty("likes_cnt")
    private Integer likesCnt;
    @JsonProperty("reg_date")
    private String regDate;
    @JsonProperty("update_date")
    private String updateDate;
    @JsonProperty("is_liked")
    private Boolean isLiked;
    @JsonProperty("notification_yn")
    private Boolean notificationYn;
    @JsonProperty("del_yn")
    private Boolean delYn;
    @JsonProperty("del_reason")
    private String delReason;
}
