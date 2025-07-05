package com.booktalk_be.domain.reply.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.reply.command.CreateReplyCommand;
import com.booktalk_be.domain.reply.command.UpdateReplyCommand;
import com.booktalk_be.common.command.RestrictCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply")
@Tag(name = "Reply API", description = "게시판 댓글 API 입니다.")
public class ReplyController {

    @GetMapping("/list/{postCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 목록 조회", description = "특정 게시글의 댓글 목록 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getList(@PathVariable String postCode) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 등록", description = "새로운 댓글을 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateReplyCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 수정", description = "댓글 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateReplyCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/restriction")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 제재", description = "관리자가 특정 댓글을 제재합니다.")
    public ResponseEntity<ResponseDto> restriction(@RequestBody @Valid RestrictCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{replyCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "게시판 댓글 삭제", description = "댓글을 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String replyCode) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mylist")
    @Tag(name = "Reply API")
    @Operation(summary = "내 댓글 조회", description = "내 댓글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommunityCommentList(@RequestParam(value = "category", required = true) String category,
                                                               @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //관리자 페이지 댓글 조회 API
    @GetMapping("/admin/list")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 댓글 관리", description = "관리자 권한으로 모든 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommentList(@RequestParam(value = "categoryId", required = true) String categoryId,
                                                      @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                      @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //관리자 페이지 댓글 복구 API
    @PostMapping("/admin/restoration/{replyCode}")
    @Tag(name = "Reply API")
    @Operation(summary = "관리자 댓글 복구", description = "관리자 권한으로 댓글을 복구합니다.")
    public ResponseEntity<ResponseDto> restoreReply(@RequestParam(value = "category", required = true) String category,
                                                    @PathVariable String replyCode)
    {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }
}
