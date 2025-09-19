package com.booktalk_be.domain.reply.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplySimpleResponse {

    @JsonProperty("reply_code")
    private String replyCode;
    @JsonProperty("post_code")
    private String postCode;
    @JsonProperty("member_id")
    private int memberId;
    private String content;
    private Boolean delYn;
    private String deleteReason;
    private String date;

}
