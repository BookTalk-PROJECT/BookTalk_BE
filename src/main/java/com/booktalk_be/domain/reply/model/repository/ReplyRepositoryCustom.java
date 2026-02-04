package com.booktalk_be.domain.reply.model.repository;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReplyRepositoryCustom {
    List<Reply> getRepliesByPostCode(String postCode);
    Page<ReplySimpleResponse> getAllRepliesForPaging(Pageable pageable);
    Page<ReplySimpleResponse> getAllRepliesForPagingByMe(Pageable pageable, int memberId);
    Page<ReplySimpleResponse> searchAllRepliesForPagingByMe(ReplySearchCondCommand cmd, Pageable pageable, int memberId);
    Page<ReplySimpleResponse> searchAllRepliesForPaging(ReplySearchCondCommand cmd, Pageable pageable);

    /**
     * Get paginated root replies (replies without parent) for a post
     */
    Page<Reply> getRootRepliesByPostCode(String postCode, Pageable pageable);

    /**
     * Get child replies for multiple parent reply codes (batch load)
     */
    List<Reply> getChildRepliesByParentCodes(List<String> parentCodes);
}
