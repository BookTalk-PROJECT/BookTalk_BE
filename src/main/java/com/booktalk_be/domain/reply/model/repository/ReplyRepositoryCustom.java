package com.booktalk_be.domain.reply.model.repository;

import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReplyRepositoryCustom {
    List<Reply> getRepliesByPostCode(String postCode);
    Page<ReplySimpleResponse> getAllRepliesForPaging(Pageable pageable);
}
