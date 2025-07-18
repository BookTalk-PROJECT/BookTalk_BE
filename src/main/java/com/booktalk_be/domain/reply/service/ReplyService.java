package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;

import java.util.List;

public interface ReplyService {
    public void createReply(CreateReplyCommand cmd);
    public List<ReplyResponse> getRepliesByPostCode(String postCode);
}
