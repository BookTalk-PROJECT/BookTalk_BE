package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.command.UpdateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBoard;
import com.booktalk_be.domain.gathering.model.repository.GatheringBoardRepository;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepository;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardPostDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.service.ReplyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GatheringBoardServiceImpl implements GatheringBoardService {

    private final GatheringBoardRepository gatheringBoardRepository;
    private final GatheringRepository gatheringRepository;
    private final ReplyService replyService;

    @Override
    public void create(CreateGatheringBoardCommand cmd, Member member) {
        Gathering gathering = gatheringRepository.findById(cmd.getGatheringCode())
                .orElseThrow(EntityNotFoundException::new);

        GatheringBoard board = GatheringBoard.builder()
                .gathering(gathering)
                .member(member)
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .notificationYn(cmd.getNotification_yn())
                .build();

        gatheringBoardRepository.save(board);
    }

    @Override
    public void modify(UpdateGatheringBoardCommand cmd) {
        GatheringBoard board = gatheringBoardRepository.findById(cmd.getPostCode())
                .orElseThrow(EntityNotFoundException::new);

        // 네 Board.modify(cmd)처럼 엔티티 메서드로 처리 추천
        board.modify(cmd);
    }

    @Override
    public void delete(String postCode) {
        GatheringBoard board = gatheringBoardRepository.findById(postCode)
                .orElseThrow(EntityNotFoundException::new);
        board.delete();
    }

    @Override
    public PageResponseDto<GatheringBoardResponse> list(String gatheringCode, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<GatheringBoardResponse> page = gatheringBoardRepository.findBoardsForPaging(gatheringCode, pageable);

        return PageResponseDto.<GatheringBoardResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public GatheringBoardDetailResponse detail(String postCode) {
        GatheringBoardPostDetailResponse detail = gatheringBoardRepository.getBoardDetailBy(postCode);
        if (detail == null) throw new EntityNotFoundException();

        List<ReplyResponse> replies = replyService.getRepliesByPostCode(detail.getBoardCode());

        return GatheringBoardDetailResponse.builder()
                .post(detail)
                .replies(replies)
                .build();
    }
}