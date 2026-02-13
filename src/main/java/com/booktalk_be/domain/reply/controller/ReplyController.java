package com.booktalk_be.domain.reply.controller;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.mypage.GatheringReplySearchCondCommand;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringReplyResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import com.booktalk_be.domain.reply.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
@Tag(name = "Reply API", description = "게시판 댓글 API 입니다.")
@Slf4j
public class ReplyController {
    private final ReplyService replyService;

    @GetMapping("/list/{postCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 목록 조회", description = "특정 게시글의 댓글 목록 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getList(
            @PathVariable String postCode,
            Authentication authentication) {
        Integer memberId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Member) {
            memberId = ((Member) authentication.getPrincipal()).getMemberId();
        }
        List<ReplyResponse> res = replyService.getRepliesByPostCode(postCode, memberId);
        return ResponseEntity.ok(ResponseDto.builder()
                .data(res)
                .code(200)
                .build());
    }

    @GetMapping("/list/{postCode}/paged")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 목록 페이징 조회", description = "특정 게시글의 댓글 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ResponseDto> getListPaged(
            @PathVariable String postCode,
            @RequestParam Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication authentication) {
        Integer memberId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Member) {
            memberId = ((Member) authentication.getPrincipal()).getMemberId();
        }
        PageResponseDto<ReplyResponse> page = replyService.getRepliesByPostCodePaginated(postCode, pageNum, pageSize, memberId);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 등록", description = "새로운 댓글을 등록합니다.")
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid CreateReplyCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        try{
            replyService.createReply(cmd, member);
        }catch (Exception e){
            log.error(e.fillInStackTrace().toString());
            return new ResponseEntity<>(ResponseDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .data("댓글 등록에 실패하였습니다.")
                    .build(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 수정", description = "댓글 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateReplyCommand cmd) {
        replyService.modifyReply(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/restrict")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 제재", description = "관리자가 특정 댓글을 제재합니다.")
    public ResponseEntity<ResponseDto> restriction(@RequestBody @Valid RestrictCommand cmd) {
        replyService.restrictReply(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{replyCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 삭제", description = "댓글을 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String replyCode) {
        replyService.deleteReply(replyCode);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mylist")
    @Tag(name = "Reply API")
    @Operation(summary = "내 커뮤니티 댓글 조회", description = "내 커뮤니티 댓글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommunityCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<ReplySimpleResponse> page =  replyService.getAllRepliesForPagingByMe(pageNum, pageSize, member.getMemberId(), "BO_");
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/mylist/search")
    @Tag(name = "Reply API")
    @Operation(summary = "내가 쓴 커뮤니티 댓글 검색", description = "내가 쓴 커뮤니티 댓글 목록을 검색합니다.")
    public ResponseEntity<ResponseDto> searchCommunityCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody @Valid ReplySearchCondCommand cmd,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<ReplySimpleResponse> page =  replyService.searchAllRepliesForPagingByMe(cmd, pageNum, pageSize, member.getMemberId(), "BO_");
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @GetMapping("/bookreview/mylist")
    @Tag(name = "Reply API")
    @Operation(summary = "내 북리뷰 댓글 조회", description = "내 북리뷰 댓글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getBookReviewCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<ReplySimpleResponse> page =  replyService.getAllRepliesForPagingByMe(pageNum, pageSize, member.getMemberId(), "BR_");
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/bookreview/mylist/search")
    @Tag(name = "Reply API")
    @Operation(summary = "내가 쓴 북리뷰 댓글 검색", description = "내가 쓴 북리뷰 댓글 목록을 검색합니다.")
    public ResponseEntity<ResponseDto> searchBookReviewCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody @Valid ReplySearchCondCommand cmd,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<ReplySimpleResponse> page =  replyService.searchAllRepliesForPagingByMe(cmd, pageNum, pageSize, member.getMemberId(), "BR_");
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    //관리자 페이지 댓글 조회 API
    @GetMapping("/admin/all")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 댓글 조회", description = "관리자 권한으로 모든 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "postType", required = false) String postType) {
        String postCodePrefix = getPostCodePrefix(postType);
        PageResponseDto<ReplySimpleResponse> page = replyService.getAllRepliesForPaging(pageNum, pageSize, postCodePrefix);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/admin/search")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 댓글 검색", description = "관리자 권한으로 모든 댓글을 검색합니다.")
    public ResponseEntity<ResponseDto> searchCommentList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "postType", required = false) String postType,
            @RequestBody @Valid ReplySearchCondCommand cmd) {
        String postCodePrefix = getPostCodePrefix(postType);
        PageResponseDto<ReplySimpleResponse> page = replyService.searchAllRepliesForPaging(cmd, pageNum, pageSize, postCodePrefix);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    private String getPostCodePrefix(String postType) {
        if (postType == null) return null;
        return switch (postType) {
            case "community" -> "BO_";
            case "bookreview" -> "BR_";
            default -> null;
        };
    }

    //관리자 페이지 댓글 복구 API
    @PatchMapping("/recover/{replyCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "관리자 댓글 복구", description = "관리자 권한으로 댓글을 복구합니다.")
    public ResponseEntity<ResponseDto> restoreReply(@PathVariable String replyCode) {
        replyService.recoverReply(replyCode);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }


    @GetMapping("/gathering/myList")
    @Operation(summary = "마이페이지 모임 댓글 조회", description = "내가 작성한 모임 댓글을 페이징 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringReplies(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringReplyResponse> page =
                replyService.getMyGatheringReplies(pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/gathering/myList/search")
    @Operation(summary = "마이페이지 모임 댓글 검색", description = "검색어/작성일(reply.reg_time) 범위로 내 모임 댓글을 검색합니다. 값이 비면 전체조회로 동작해야 합니다.")
    public ResponseEntity<ResponseDto> searchMyGatheringReplies(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody GatheringReplySearchCondCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringReplyResponse> page =
                replyService.searchMyGatheringReplies(cmd, pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }
}
