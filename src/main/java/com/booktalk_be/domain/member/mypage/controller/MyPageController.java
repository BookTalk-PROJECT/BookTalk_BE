package com.booktalk_be.domain.member.mypage.controller;


import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mypage")
@Tag(name = "MyPage API", description = "마이 페이지 API 입니다.")
public class MyPageController {

    @GetMapping("/main")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 최근 활동", description = "마이 페이지 메인의 내 최근 활동 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getRecentActivityList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                         @RequestBody @Valid PostSearchCondCommand cmd)  {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/bookreview/board")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 북리뷰 게시글 관리", description = "마이 페이지의 북리뷰 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReviewBoardList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                         @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/bookreview/comment")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 북리뷰 댓글 관리", description = "마이 페이지의 북리뷰 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReviewCommentList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                              @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/community/board")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 커뮤니티 게시글 관리", description = "마이 페이지의 커뮤니티 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommunityBoardList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                              @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/community/comment")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 커뮤니티 댓글 관리", description = "마이 페이지의 커뮤니티 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommunityCommentList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                             @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/gathering/mygathering")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임", description = "마이 페이지의 내 모임들을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/gathering/board")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 게시물 관리", description = "마이 페이지의 내 모임 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringBoardList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/gathering/comment")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 댓글 관리", description = "마이 페이지의 내 모임 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringCommentList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/gathering/manage/request")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 신청 관리", description = "마이 페이지의 모임 신청 관리 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringRequestList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/gathering/manage/approval")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 승인 관리", description = "마이 페이지의 내 모임 승인 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringApprovalList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    }
