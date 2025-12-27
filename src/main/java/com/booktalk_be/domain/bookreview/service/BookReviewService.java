package com.booktalk_be.domain.bookreview.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.bookreview.command.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewDetailDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.command.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.command.UpdateBookReviewCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import org.springframework.data.domain.Pageable;

public interface BookReviewService {
    String createBookReview(Member member, CreateBookReviewCommand createBookReviewCommand);
    PageResponseDto<BookReviewListDto> getBookReviewList(Integer categoryId, Pageable pageable);
    PageResponseDto<BookReviewListDto> searchBookReviews(Integer categoryId, BookReviewSearchCondCommand cmd, Pageable pageable);
    BookReviewDetailDto getBookReview(String bookReviewId);
    void updateBookReview(String bookReviewId, UpdateBookReviewCommand updateBookReviewCommand);
    void deleteBookReview(String bookReviewId);
}
