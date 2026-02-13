package com.booktalk_be.domain.bookreview.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.bookreview.command.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewAdminListDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewDetailDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.command.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.command.UpdateBookReviewCommand;
import com.booktalk_be.domain.bookreview.model.entity.BookReview;
import com.booktalk_be.domain.bookreview.model.repository.BookReviewRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final MemberRepository memberRepository;

    @Override
    public String createBookReview(Member member, CreateBookReviewCommand cmd) {
        BookReview bookReview = BookReview.builder()
                .categoryId(cmd.getCategoryId())
                .member(member)
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .bookTitle(cmd.getBookTitle())
                .authors(cmd.getAuthors())
                .publisher(cmd.getPublisher())
                .isbn(cmd.getIsbn())
                .thumbnailUrl(cmd.getThumbnailUrl())
                .rating(cmd.getRating())
                .build();

        bookReviewRepository.save(bookReview);
        return bookReview.getCode();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> getBookReviewList(Integer categoryId, Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.findForPaging(categoryId, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> searchBookReviews(Integer categoryId, BookReviewSearchCondCommand cmd, Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.searchByCondition(categoryId, cmd, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewDetailDto getBookReview(String bookReviewId) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));

        BookReviewDetailDto post = BookReviewDetailDto.builder()
                .code(bookReview.getCode())
                .memberId(bookReview.getMember().getMemberId())
                .title(bookReview.getTitle())
                .content(bookReview.getContent())
                .author(bookReview.getMember().getName())
                .views(bookReview.getViews())
                .likesCnt(bookReview.getLikesCnt())
                .regDate(bookReview.getRegTime().toString())
                .updateDate(bookReview.getUpdateTime().toString())
//                .isLiked(false) // Assuming not liked for now
                .delYn(bookReview.getDelYn())
                .delReason(bookReview.getDelReason())
                .bookTitle(bookReview.getBookTitle())
                .authors(bookReview.getAuthors())
                .publisher(bookReview.getPublisher())
                .isbn(bookReview.getIsbn())
                .thumbnailUrl(bookReview.getThumbnailUrl())
                .rating(bookReview.getRating())
                .build();

        return post; // No replies for now
    }

    @Override
    public void updateBookReview(String bookReviewId, UpdateBookReviewCommand cmd) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.modify(cmd);
    }

    @Override
    public void deleteBookReview(String bookReviewId) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.delete();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> getMyBookReviewList(int memberId, Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.findMyBookReviewsForPaging(memberId, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewListDto> searchMyBookReviews(int memberId, BookReviewSearchCondCommand cmd, Pageable pageable) {
        Page<BookReviewListDto> page = bookReviewRepository.searchMyBookReviews(memberId, cmd, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewAdminListDto> getBookReviewListForAdmin(Pageable pageable) {
        Page<BookReviewAdminListDto> page = bookReviewRepository.findAllForAdminPaging(pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookReviewAdminListDto> searchBookReviewsForAdmin(BookReviewSearchCondCommand cmd, Pageable pageable) {
        Page<BookReviewAdminListDto> page = bookReviewRepository.searchAllForAdmin(cmd, pageable);
        return new PageResponseDto<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public void restrictBookReview(String bookReviewId, String delReason) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.restrict(delReason);
    }

    @Override
    public void recoverBookReview(String bookReviewId) {
        BookReview bookReview = bookReviewRepository.findById(bookReviewId)
                .orElseThrow(() -> new EntityNotFoundException("BookReview not found with id: " + bookReviewId));
        bookReview.recover();
    }
}
