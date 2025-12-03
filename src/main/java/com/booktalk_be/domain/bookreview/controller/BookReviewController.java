package com.booktalk_be.domain.bookreview.controller;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewDetailResponse;
import com.booktalk_be.domain.bookreview.dto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.dto.CreateBookReviewCommand;
import com.booktalk_be.domain.bookreview.dto.BookReviewSearchCondCommand; // Add this import
import com.booktalk_be.domain.bookreview.dto.UpdateBookReviewCommand;
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
@Tag(name = "Book Review API", description = "서평 API 입니다.")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    @PostMapping
    @Operation(summary = "서평 등록", description = "새로운 서평을 등록합니다.")
    public ResponseEntity<ResponseDto> createBookReview(
            @RequestBody @Valid CreateBookReviewCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        String bookReviewId = bookReviewService.createBookReview(member.getEmail(), cmd);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewId).build());
    }

    @GetMapping
    @Operation(summary = "서평 목록 조회", description = "서평 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReviewList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page-1, size);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.getBookReviewList(pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }

    @PostMapping("/search") // New search endpoint
    @Operation(summary = "서평 검색", description = "검색 조건에 맞는 서평 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> searchBookReviews(
            @RequestBody @Valid BookReviewSearchCondCommand cmd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page-1, size);
        PageResponseDto<BookReviewListDto> bookReviewList = bookReviewService.searchBookReviews(cmd, pageable);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReviewList).build());
    }


    @GetMapping("/{bookReviewId}")
    @Operation(summary = "서평 상세 조회", description = "서평 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReview(
            @PathVariable String bookReviewId
    ) {
        BookReviewDetailResponse bookReview = bookReviewService.getBookReview(bookReviewId);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").data(bookReview).build());
    }

    @PutMapping("/{bookReviewId}")
    @Operation(summary = "서평 수정", description = "서평을 수정합니다.")
    public ResponseEntity<ResponseDto> updateBookReview(
            @PathVariable String bookReviewId,
            @RequestBody @Valid UpdateBookReviewCommand cmd
    ) {
        bookReviewService.updateBookReview(bookReviewId, cmd);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }

    @DeleteMapping("/{bookReviewId}")
    @Operation(summary = "서평 삭제", description = "서평을 삭제합니다.")
    public ResponseEntity<ResponseDto> deleteBookReview(
            @PathVariable String bookReviewId
    ) {
        bookReviewService.deleteBookReview(bookReviewId);
        return ResponseEntity.ok(ResponseDto.builder().code(200).msg("Success").build());
    }
}
