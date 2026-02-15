package com.booktalk_be.domain.bookreview.model.repository;

import com.booktalk_be.domain.bookreview.command.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewAdminListDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewRepositoryCustom {
    Page<BookReviewListDto> findForPaging(Integer categoryId, Pageable pageable);
    Page<BookReviewListDto> searchByCondition(Integer categoryId, BookReviewSearchCondCommand cmd, Pageable pageable);
    Page<BookReviewListDto> findMyBookReviewsForPaging(int memberId, Pageable pageable);
    Page<BookReviewListDto> searchMyBookReviews(int memberId, BookReviewSearchCondCommand cmd, Pageable pageable);

    // Admin methods
    Page<BookReviewAdminListDto> findAllForAdminPaging(Pageable pageable);
    Page<BookReviewAdminListDto> searchAllForAdmin(BookReviewSearchCondCommand cmd, Pageable pageable);
}
