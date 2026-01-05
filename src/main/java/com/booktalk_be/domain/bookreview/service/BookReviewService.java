package com.booktalk_be.domain.bookreview.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewDetailResponse;
import com.booktalk_be.domain.bookreview.dto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.dto.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.dto.UpdateBookReviewCommand;
import com.booktalk_be.domain.bookreview.dto.BookReviewSearchCondCommand; // Import the new command
import org.springframework.data.domain.Pageable;

public interface BookReviewService {
    String createBookReview(String userEmail, CreateBookReviewCommand createBookReviewCommand);
    PageResponseDto<BookReviewListDto> getBookReviewList(Pageable pageable);
    PageResponseDto<BookReviewListDto> searchBookReviews(BookReviewSearchCondCommand cmd, Pageable pageable);
    BookReviewDetailResponse getBookReview(String bookReviewId);
    void updateBookReview(String bookReviewId, UpdateBookReviewCommand updateBookReviewCommand);
    void deleteBookReview(String bookReviewId);
}
