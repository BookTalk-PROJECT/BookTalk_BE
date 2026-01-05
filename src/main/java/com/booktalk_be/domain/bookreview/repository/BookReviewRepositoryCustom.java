package com.booktalk_be.domain.bookreview.repository;

import com.booktalk_be.domain.bookreview.dto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewSearchCondCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewRepositoryCustom {
    Page<BookReviewListDto> findByCondition(Pageable pageable);
    Page<BookReviewListDto> searchByCondition(BookReviewSearchCondCommand cmd, Pageable pageable);
}
