package com.booktalk_be.domain.bookreview.controller;

import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.bookreview.command.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewAdminListDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewDetailDto;
import com.booktalk_be.domain.bookreview.responseDto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.command.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.command.UpdateBookReviewCommand;
import com.booktalk_be.domain.bookreview.service.BookReviewService;
import com.booktalk_be.domain.member.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book-reviews") // Corrected RequestMapping
@RequiredArgsConstructor
@Tag(name = "Book Review API", description = "책리뷰 API 입니다.")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    @PostMapping
    @Operation(summary = "책리뷰 등록", description = "새로운 책리뷰를 등록합니다.")
    public ResponseEntity<ResponseDto> createBookReview(
            @RequestBody CreateBookReviewCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        String bookReviewId = bookReviewService.createBookReview(member, cmd);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewId).build());
    }

    @GetMapping
    @Operation(summary = "서평 목록 조회", description = "서평 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReviewList(
            @RequestParam(value = "categoryId", required = true) Integer categoryId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.getBookReviewList(categoryId, pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    @PostMapping("/search") // New search endpoint
    @Operation(summary = "서평 검색", description = "검색 조건에 맞는 서평 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> searchBookReviews(
            @RequestParam(value = "categoryId", required = true) Integer categoryId,
            @RequestBody BookReviewSearchCondCommand cmd,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.searchBookReviews(categoryId, cmd, pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }


    @GetMapping("/{bookReviewId}")
    @Operation(summary = "서평 상세 조회", description = "서평 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReview(
            @PathVariable String bookReviewId
    ) {
        BookReviewDetailDto bookReview = bookReviewService.getBookReview(bookReviewId);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReview).build());
    }

    @PutMapping("/{bookReviewId}")
    @Operation(summary = "서평 수정", description = "서평을 수정합니다.")
    public ResponseEntity<ResponseDto> updateBookReview(
            @PathVariable String bookReviewId,
            @RequestBody @Valid UpdateBookReviewCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        bookReviewService.updateBookReview(bookReviewId, cmd, member.getMemberId());
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }

    @DeleteMapping("/{bookReviewId}")
    @Operation(summary = "서평 삭제", description = "서평을 삭제합니다.")
    public ResponseEntity<ResponseDto> deleteBookReview(
            @PathVariable String bookReviewId,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        bookReviewService.deleteBookReview(bookReviewId, member.getMemberId());
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }

    @GetMapping("/mylist")
    @Operation(summary = "내 서평 목록 조회", description = "내가 작성한 서평 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyBookReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.getMyBookReviewList(member.getMemberId(), pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    @PostMapping("/mylist/search")
    @Operation(summary = "내 서평 검색", description = "내가 작성한 서평 목록을 검색합니다.")
    public ResponseEntity<ResponseDto> searchMyBookReviews(
            @RequestBody BookReviewSearchCondCommand cmd,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.searchMyBookReviews(member.getMemberId(), cmd, pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    // Admin APIs
    @GetMapping("/admin/all")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 북리뷰 조회", description = "관리자 권한으로 모든 북리뷰를 조회합니다.")
    public ResponseEntity<ResponseDto> getAdminBookReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        PageResponseDto<BookReviewAdminListDto> bookReviewList = bookReviewService.getBookReviewListForAdmin(pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    @PostMapping("/admin/search")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 북리뷰 검색", description = "관리자 권한으로 북리뷰를 검색합니다.")
    public ResponseEntity<ResponseDto> searchAdminBookReviews(
            @RequestBody BookReviewSearchCondCommand cmd,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        PageResponseDto<BookReviewAdminListDto> bookReviewList = bookReviewService.searchBookReviewsForAdmin(cmd, pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    @PatchMapping("/restrict")
    @Tag(name = "AdminPage API")
    @Operation(summary = "북리뷰 제재", description = "관리자가 북리뷰를 제재합니다.")
    public ResponseEntity<ResponseDto> restrictBookReview(@RequestBody @Valid RestrictCommand cmd) {
        bookReviewService.restrictBookReview(cmd.getTargetCode(), cmd.getDelReason());
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }

    @PatchMapping("/recover/{bookReviewId}")
    @Tag(name = "AdminPage API")
    @Operation(summary = "북리뷰 복구", description = "관리자가 북리뷰를 복구합니다.")
    public ResponseEntity<ResponseDto> recoverBookReview(@PathVariable String bookReviewId) {
        bookReviewService.recoverBookReview(bookReviewId);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }
}
