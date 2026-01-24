package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.command.UpdateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.command.mypage.GatheringBoardSearchCondCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBoard;
import com.booktalk_be.domain.gathering.model.repository.GatheringBoardRepository;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepository;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardPostDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringBoardResponse;
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

import java.math.BigInteger;
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


    @Override
    public PageResponseDto<MyPageGatheringBoardResponse> getMyGatheringBoards(Integer pageNum, Integer pageSize, int memberId) {
        List<Object[]> rows = gatheringBoardRepository.callMyGatheringBoardList(memberId, pageNum, pageSize);
        return buildPage(rows);
    }

    @Override
    public PageResponseDto<MyPageGatheringBoardResponse> searchMyGatheringBoards(GatheringBoardSearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId) {
        List<Object[]> rows = gatheringBoardRepository.callMyGatheringBoardSearch(
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

    private PageResponseDto<MyPageGatheringBoardResponse> buildPage(List<Object[]> rows) {
        int totalPages = 0;

        if (rows != null && !rows.isEmpty()) {
            Object[] first = rows.get(0);
            totalPages = toInt(first[first.length - 1]); // 마지막 컬럼 = total_pages
        }

        List<MyPageGatheringBoardResponse> content =
                (rows == null) ? List.of() : rows.stream().map(this::mapRow).toList();

        return PageResponseDto.<MyPageGatheringBoardResponse>builder()
                .content(content)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 프로시저 컬럼 순서(권장):
     * 0 board_code
     * 1 gathering_name
     * 2 title
     * 3 author
     * 4 del_yn
     * 5 reg_date
     * 6 total_pages (마지막)
     */
    private MyPageGatheringBoardResponse mapRow(Object[] r) {
        return MyPageGatheringBoardResponse.builder()
                .boardCode(r[0] == null ? null : String.valueOf(r[0]))
                .gatheringName(r[1] == null ? null : String.valueOf(r[1]))
                .title(r[2] == null ? null : String.valueOf(r[2]))
                .author(r[3] == null ? null : String.valueOf(r[3]))
                .delYn(toInt(r[4]))
                .regDate(r[5] == null ? null : String.valueOf(r[5]))
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