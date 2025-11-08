package com.booktalk_be.domain.board.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.validation.Valid;

public interface BoardService {

    public void createBoard(CreateBoardCommand cmd, Member member);
    public void modifyBoard(UpdateBoardCommand cmd);
    public void restrictBoard(RestrictCommand cmd);
    public void recoverBoard(String boardCode);
    public void deleteBoard(String boardCode);
    public PageResponseDto<BoardResponse> getBoardsForPaging(Integer categoryId, Integer pageNum, Integer pageSize);
    public PageResponseDto<BoardResponse> searchBoardsForPaging(Integer categoryId, Integer pageNum, Integer pageSize, PostSearchCondCommand cnd);
    public BoardDetailResponse getBoardDetail(String boardCode);
    public PageResponseDto<BoardResponse> getAllBoardsForPaging(Integer pageNum, Integer pageSize);
    PageResponseDto<BoardResponse> getAllBoardsForPagingByMe(Integer pageNum, Integer pageSize, int memberId);
    PageResponseDto<BoardResponse> searchAllBoardsForPagingByMe(PostSearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId);
    PageResponseDto<BoardResponse> searchAllBoardsForPaging(PostSearchCondCommand cmd, Integer pageNum, Integer pageSize, int memberId);

    String queryNextBoard(String boardCode, Integer categoryId);
    String queryPrevBoard(String boardCode, Integer categoryId);
}
