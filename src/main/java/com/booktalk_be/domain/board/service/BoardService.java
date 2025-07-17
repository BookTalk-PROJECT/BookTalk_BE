package com.booktalk_be.domain.board.service;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

public interface BoardService {

    public void createBoard(CreateBoardCommand cmd);
    public void modifyBoard(UpdateBoardCommand cmd);
    public void restrictBoard(RestrictCommand cmd);
    public void deleteBoard(String boardCode);
    public PageResponseDto<BoardResponse> getBoardsForPaging(Integer categoryId, Integer pageNum, Integer pageSize);
    public Page<BoardResponse> searchBoardsForPaging(String categoryId, Integer pageNum, Integer pageSize, PostSearchCondCommand cnd);
    public BoardDetailResponse getBoardDetail(String boardCode);
}
