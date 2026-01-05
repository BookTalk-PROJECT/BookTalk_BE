package com.booktalk_be.domain.bookreview.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewDetailDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewDetailResponse;
import com.booktalk_be.domain.bookreview.dto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.dto.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.dto.UpdateBookReviewCommand;
import com.booktalk_be.domain.bookreview.dto.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.entity.BookReview;
import com.booktalk_be.domain.bookreview.repository.BookReviewRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final MemberRepository memberRepository;

    @Override
    public String createBookReview(String userEmail, CreateBookReviewCommand cmd) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with email: " + userEmail));

        BookReview bookReview = BookReview.builder()
                .member(member)
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .bookTitle(cmd.getBookTitle())
                .authors(cmd.getAuthors())
                .publisher(cmd.getPublisher())
                .isbn(cmd.getIsbn())
                .thumbnail(cmd.getThumbnail())
                .rating(cmd.getRating())
                .notificationYn(cmd.getNotificationYn())
                .build();

        bookReviewRepository.save(bookReview);
        return bookReview.getCode();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> getBookReviewList(Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.findByCondition(pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> searchBookReviews(BookReviewSearchCondCommand cmd, Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.searchByCondition(cmd, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewDetailResponse getBookReview(String bookReviewId) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));

        BookReviewDetailDto post = BookReviewDetailDto.builder()
                .boardCode(bookReview.getCode())
                .memberId(bookReview.getMember().getMemberId())
                .title(bookReview.getTitle())
                .content(bookReview.getContent())
                .author(bookReview.getMember().getName())
                .views(bookReview.getViews())
                .likesCnt(bookReview.getLikesCnt())
                .regDate(bookReview.getRegTime().toString())
                .updateDate(bookReview.getUpdateTime().toString())
                .isLiked(false) // Assuming not liked for now
                .notificationYn(bookReview.getNotificationYn())
                .delYn(bookReview.getDelYn())
                .delReason(bookReview.getDelReason())
                .bookTitle(bookReview.getBookTitle())
                .authors(bookReview.getAuthors())
                .publisher(bookReview.getPublisher())
                .isbn(bookReview.getIsbn())
                .thumbnail(bookReview.getThumbnail())
                .rating(bookReview.getRating())
                .build();

        return new BookReviewDetailResponse(post, Collections.emptyList()); // No replies for now
    }

    @Override
    public void updateBookReview(String bookReviewId, UpdateBookReviewCommand cmd) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.modify(cmd);
        bookReviewRepository.save(bookReview);
    }

    @Override
    public void deleteBookReview(String bookReviewId) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.delete();
        bookReviewRepository.save(bookReview);
    }
}
