package com.booktalk_be.domain.reply.service;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.mypage.GatheringReplySearchCondCommand;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringReplyResponse;
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

import java.math.BigInteger;
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
                                .memberName(entity.getMember().getName())
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

    @Override
    public PageResponseDto<MyPageGatheringReplyResponse> getMyGatheringReplies(Integer pageNum, Integer pageSize, int memberId) {
        List<Object[]> rows = replyRepository.callMyGatheringReplyList(memberId, pageNum, pageSize);
        return buildPage(rows);
    }

    @Override
    public PageResponseDto<MyPageGatheringReplyResponse> searchMyGatheringReplies(GatheringReplySearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId) {
        List<Object[]> rows = replyRepository.callMyGatheringReplySearch(
                memberId,
                emptyToNull(cmd.getKeywordType()),
                emptyToNull(cmd.getKeyword()),
                emptyToNull(cmd.getStartDate()),
                emptyToNull(cmd.getEndDate()),
                pageNum,
                pageSize
        );
        return buildPage(rows);
    }

    private PageResponseDto<MyPageGatheringReplyResponse> buildPage(List<Object[]> rows) {
        int totalPages = 0;
        if (rows != null && !rows.isEmpty()) {
            Object[] first = rows.get(0);
            totalPages = toInt(first[first.length - 1]); // 마지막 = total_pages
        }

        List<MyPageGatheringReplyResponse> content =
                (rows == null) ? List.of() : rows.stream().map(this::mapRow).toList();

        return PageResponseDto.<MyPageGatheringReplyResponse>builder()
                .content(content)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 프로시저 컬럼 순서(권장):
     * 0 reply_code
     * 1 post_code
     * 2 gathering_name
     * 3 post_title
     * 4 content
     * 5 author
     * 6 del_yn
     * 7 reg_date
     * 8 total_pages (마지막)
     */
    private MyPageGatheringReplyResponse mapRow(Object[] r) {
        return MyPageGatheringReplyResponse.builder()
                .replyCode(r[0] == null ? null : String.valueOf(r[0]))
                .postCode(r[1] == null ? null : String.valueOf(r[1]))
                .gatheringName(r[2] == null ? null : String.valueOf(r[2]))
                .postTitle(r[3] == null ? null : String.valueOf(r[3]))
                .content(r[4] == null ? null : String.valueOf(r[4]))
                .author(r[5] == null ? null : String.valueOf(r[5]))
                .delYn(toInt(r[6]))
                .regDate(r[7] == null ? null : String.valueOf(r[7]))
                .build();
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof BigInteger bi) return bi.intValue();
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }
}
