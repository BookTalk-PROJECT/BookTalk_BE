package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReplyService {
    public void createReply(CreateReplyCommand cmd);
    public void modifyReply(UpdateReplyCommand cmd);
    public void deleteReply(String replyCode);
    public List<ReplyResponse> getRepliesByPostCode(String postCode);
    public PageResponseDto<ReplySimpleResponse> getAllRepliesForPaging(int pageNum, int pageSize);
    public void restrictReply(RestrictCommand cmd);
    public void recoverReply(String replyCode);
}
