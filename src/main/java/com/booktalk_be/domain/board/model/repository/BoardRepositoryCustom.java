package com.booktalk_be.domain.board.model.repository;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.board.responseDto.CommuDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {

    Page<BoardResponse> findBoardsForPaging(Integer categoryId, Pageable pageable);
    Page<BoardResponse> searchBoardsForPaging(Integer categoryId, Pageable pageable, PostSearchCondCommand cmd);
    CommuDetailResponse getBoardDetailBy(String boardCode);
    Page<BoardResponse> getAllBoardsForPaging(Pageable pageable);
}
