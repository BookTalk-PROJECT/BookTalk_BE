package com.booktalk_be.domain.board.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.board.model.repository.BoardRepository;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.board.responseDto.CommuDetailResponse;
import com.booktalk_be.domain.likes.model.repository.LikesRepository;
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
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ReplyService replyService;
    private final LikesRepository likesRepository;

    @Override
    public void createBoard(CreateBoardCommand cmd, Member member) {
        Board board = Board.builder()
                .categoryId(cmd.getCategoryId())
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .notificationYn(cmd.getNotification_yn())
                .member(member)
                .build();

        boardRepository.save(board);
    }

    @Override
    public void modifyBoard(UpdateBoardCommand cmd) {
        Board board = boardRepository.findById(cmd.getBoardCode())
                .orElseThrow(EntityNotFoundException::new);
        board.modify(cmd);
    }

    @Override
    public void restrictBoard(RestrictCommand cmd) {
        Board board = boardRepository.findById(cmd.getTargetCode())
                .orElseThrow(EntityNotFoundException::new);
        board.delete(cmd.getDelReason());
    }

    @Override
    public void recoverBoard(String boardCode) {
        Board board = boardRepository.findById(boardCode)
                .orElseThrow(EntityNotFoundException::new);
        board.recover();
    }

    @Override
    public void deleteBoard(String boardCode) {
        Board board = boardRepository.findById(boardCode)
                .orElseThrow(EntityNotFoundException::new);
        board.delete();
    }

    @Override
    public PageResponseDto<BoardResponse> getBoardsForPaging(
            Integer categoryId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<BoardResponse> page = boardRepository.findBoardsForPaging(categoryId, pageable);
        return PageResponseDto.<BoardResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public PageResponseDto<BoardResponse> searchBoardsForPaging(
            Integer categoryId, Integer pageNum, Integer pageSize, PostSearchCondCommand cnd) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<BoardResponse> page = boardRepository.searchBoardsForPaging(categoryId, pageable, cnd);
        return PageResponseDto.<BoardResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public BoardDetailResponse getBoardDetail(String boardCode) {
        CommuDetailResponse detail = boardRepository.getBoardDetailBy(boardCode);
        List<ReplyResponse> replies = replyService.getRepliesByPostCode(detail.getBoardCode());
        //TODO 좋아요 기능 추가 후 활성화
//        Boolean isLikedByMe = likesRepository.isLikedAtBoardBy(boardCode, userId);
        return BoardDetailResponse.builder()
                .post(detail)
                .replies(replies)
                .build();
    }

    @Override
    public PageResponseDto<BoardResponse> getAllBoardsForPaging(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<BoardResponse> page =  boardRepository.getAllBoardsForPaging(pageable);
        return PageResponseDto.<BoardResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public PageResponseDto<BoardResponse> getAllBoardsForPagingByMe(Integer pageNum, Integer pageSize, int memberId) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        Page<BoardResponse> page =  boardRepository.getAllBoardsForPagingByMe(pageable, memberId);
        return PageResponseDto.<BoardResponse>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .build();
    }
}
