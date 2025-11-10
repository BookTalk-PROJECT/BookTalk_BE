package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepository;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;

    @Override
    public void createReply(CreateReplyCommand cmd, Member member) {
        Reply reply;
        if (cmd.getParentReplyCode() == null) {
             reply = Reply.builder()
                     .postCode(cmd.getPostCode())
                     .content(cmd.getContent())
                     .member(member)
                     .build();
        }else {
            Reply pReply = replyRepository.findById(cmd.getParentReplyCode())
                    .orElseThrow(EntityNotFoundException::new);
            reply = Reply.builder()
                    .postCode(cmd.getPostCode())
                    .content(cmd.getContent())
                    .parentReplyCode(pReply)
                    .member(member)
                    .build();
        }
        replyRepository.save(reply);
    }

    @Override
    public void modifyReply(UpdateReplyCommand cmd) {
        Reply reply =  replyRepository.findById(cmd.getReplyCode())
                .orElseThrow(EntityNotFoundException::new);
        reply.modify(cmd);
    }

    @Override
    public void deleteReply(String replyCode) {
        Reply reply = replyRepository.findById(replyCode)
                .orElseThrow(EntityNotFoundException::new);
        reply.delete();
    }

    @Override
    public List<ReplyResponse> getRepliesByPostCode(String postCode) {
        List<Reply> replies = replyRepository.getRepliesByPostCode(postCode);
        Map<String, ReplyResponse> nodeMap = replies.stream()
                .collect(Collectors.toMap(
                        Reply::getReplyCode,
                        (entity) -> ReplyResponse.builder()
                                .replyCode(entity.getReplyCode())
//                                .memberId(getMemberId(entity))
                                .postCode(entity.getPostCode())
                                .content(entity.getContent())
                                .regDate(entity.getRegTime().toLocalDate().toString()) // 연도-월-일 문자열로
                                .updateDate(entity.getUpdateTime().toLocalDate().toString())
//                                .likesCnt(entity.getLikesCnt())
//                                .isLiked(false) // 좋아요 여부는 실제 로직 필요
                                .replies(new ArrayList<>()) // 빈 리스트로 초기화
                                .build(),
                        (a, b) -> a // 중복 키가 있을 경우 먼저 값 사용
                ));
        replies.forEach(reply -> {
            Reply parent = reply.getParentReplyCode();
            if (parent != null) {
                ReplyResponse parentDto = nodeMap.get(parent.getReplyCode());
                if (parentDto != null) {
                    ReplyResponse childDto = nodeMap.get(reply.getReplyCode());
                    parentDto.getReplies().add(childDto);
                }
            }
        });

        return replies.stream()
                .filter(reply -> reply.getParentReplyCode() == null)
                .map(reply -> nodeMap.get(reply.getReplyCode()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDto<ReplySimpleResponse> getAllRepliesForPaging(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<ReplySimpleResponse> page =  replyRepository.getAllRepliesForPaging(pageable);
        return PageResponseDto.<ReplySimpleResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public void restrictReply(RestrictCommand cmd) {
        Reply reply = replyRepository.findById(cmd.getTargetCode())
                .orElseThrow(EntityNotFoundException::new);
        reply.delete(cmd.getDelReason());
    }

    @Override
    public void recoverReply(String replyCode) {
        Reply reply = replyRepository.findById(replyCode)
                .orElseThrow(EntityNotFoundException::new);
        reply.recover();
    }

    @Override
    public PageResponseDto<ReplySimpleResponse> getAllRepliesForPagingByMe(Integer pageNum, Integer pageSize, int memberId) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<ReplySimpleResponse> page =  replyRepository.getAllRepliesForPagingByMe(pageable, memberId);
        return PageResponseDto.<ReplySimpleResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public PageResponseDto<ReplySimpleResponse> searchAllRepliesForPagingByMe(ReplySearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<ReplySimpleResponse> page =  replyRepository.searchAllRepliesForPagingByMe(cmd, pageable, memberId);
        return PageResponseDto.<ReplySimpleResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public PageResponseDto<ReplySimpleResponse> searchAllRepliesForPaging(ReplySearchCondCommand cmd, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<ReplySimpleResponse> page =  replyRepository.searchAllRepliesForPaging(cmd, pageable);
        return PageResponseDto.<ReplySimpleResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }
}
