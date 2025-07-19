package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepository;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;

    @Override
    public void createReply(CreateReplyCommand cmd) {
        Reply reply;
        if (cmd.getParentReplyCode() == null) {
             reply = Reply.builder()
                     .postCode(cmd.getPostCode())
                     .content(cmd.getContent())
                     .build();
        }else {
            Reply pReply = replyRepository.findById(cmd.getParentReplyCode())
                    .orElseThrow(EntityNotFoundException::new);
            reply = Reply.builder()
                    .postCode(cmd.getPostCode())
                    .content(cmd.getContent())
                    .parentReplyCode(pReply)
                    .build();
        }
        replyRepository.save(reply);
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
}
