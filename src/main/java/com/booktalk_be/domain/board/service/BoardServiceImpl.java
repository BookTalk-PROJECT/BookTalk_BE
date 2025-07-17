package com.booktalk_be.domain.board.service;

import com.booktalk_be.common.baseEntity.Post;
import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.board.model.repository.BoardRepository;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.likes.model.repository.LikesRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final LikesRepository likesRepository;

    @Override
    public void createBoard(CreateBoardCommand cmd) {
        Board board = Board.builder()
                .categoryId(cmd.getCategoryId())
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .notificationYn(cmd.getNotification_yn())
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
    public Page<BoardResponse> searchBoardsForPaging(
            String categoryId, Integer pageNum, Integer pageSize, PostSearchCondCommand cnd) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return boardRepository.searchBoardsForPaging(categoryId, pageable, cnd);
    }

    @Override
    public BoardDetailResponse getBoardDetail(String boardCode) {
        Board board = boardRepository.findById(boardCode)
                .orElseThrow(EntityNotFoundException::new);
//        Boolean isLikedByMe = likesRepository.isLikedAtBoardBy(boardCode, userId);
        return BoardDetailResponse.builder()
                .boardCode(board.getCode())
                .memberId(board.getMember().getMemberId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getMember().getName())
                .views(board.getViews())
                .likesCnt(board.getLikeCnt())
                .regDate(board.getRegTime().toLocalDate())
                .updateDate(board.getUpdateTime())
//                .isLiked()
                .notificationYn(board.getNotificationYn())
                .build();
    }
}
