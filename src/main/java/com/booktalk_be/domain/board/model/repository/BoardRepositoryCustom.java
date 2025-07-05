package com.booktalk_be.domain.board.model.repository;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {

    Page<BoardResponse> findBoardsForPaging(String categoryId, Pageable pageable);
    Page<BoardResponse> searchBoardsForPaging(String categoryId, Pageable pageable, PostSearchCondCommand cmd);

}
