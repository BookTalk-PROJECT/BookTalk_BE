package com.booktalk_be.domain.bookreview.model.repository;

import com.booktalk_be.domain.bookreview.command.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewRepositoryCustom {
    Page<BookReviewListDto> findForPaging(Integer categoryId, Pageable pageable);
    Page<BookReviewListDto> searchByCondition(Integer categoryId, BookReviewSearchCondCommand cmd, Pageable pageable);
}
