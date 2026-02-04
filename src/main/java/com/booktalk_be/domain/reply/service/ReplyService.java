package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.mypage.GatheringReplySearchCondCommand;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringReplyResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;

import java.util.List;

public interface ReplyService {
    public void createReply(CreateReplyCommand cmd, Member member);
    public void modifyReply(UpdateReplyCommand cmd);
    public void deleteReply(String replyCode);
    public List<ReplyResponse> getRepliesByPostCode(String postCode);
    public PageResponseDto<ReplySimpleResponse> getAllRepliesForPaging(int pageNum, int pageSize);
    public void restrictReply(RestrictCommand cmd);
    public void recoverReply(String replyCode);
    public PageResponseDto<ReplySimpleResponse> getAllRepliesForPagingByMe(Integer pageNum, Integer pageSize, int memberId);
    PageResponseDto<ReplySimpleResponse> searchAllRepliesForPagingByMe(ReplySearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId);
    PageResponseDto<ReplySimpleResponse> searchAllRepliesForPaging(ReplySearchCondCommand cmd, Integer pageNum, Integer pageSize);

    PageResponseDto<MyPageGatheringReplyResponse> getMyGatheringReplies(Integer pageNum, Integer pageSize, int memberId);
    PageResponseDto<MyPageGatheringReplyResponse> searchMyGatheringReplies(GatheringReplySearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId);

    /**
     * Get paginated replies by post code with nested tree structure
     * Root replies are paginated, child replies are fetched in batch
     */
    PageResponseDto<ReplyResponse> getRepliesByPostCodePaginated(String postCode, Integer pageNum, Integer pageSize);

}
