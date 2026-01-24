package com.booktalk_be.domain.gathering.responseDto.mypage;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageGatheringReplyResponse {

    @JsonProperty("reply_code")
    private String replyCode;

    @JsonProperty("post_code")
    private String postCode;

    @JsonProperty("gathering_name")
    private String gatheringName;

    @JsonProperty("post_title")
    private String postTitle;

    @JsonProperty("content")
    private String content;

    @JsonProperty("author")
    private String author;

    @JsonProperty("del_yn")
    private Integer delYn; // 0/1

    @JsonProperty("reg_date")
    private String regDate; // yyyy-MM-dd
}