package com.booktalk_be.domain.reply.model.repository;

import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;

import java.util.List;

public interface ReplyRepositoryCustom {
    List<Reply> getRepliesByPostCode(String postCode);
}
