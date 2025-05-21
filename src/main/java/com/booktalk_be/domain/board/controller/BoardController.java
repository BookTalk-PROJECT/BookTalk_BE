package com.booktalk_be.domain.board.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/board")
@Tag(name = "Community Board API", description = "커뮤니티 게시판 API 입니다.")
public class BoardController {

    @GetMapping("/list")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 목록 조회", description = "검색 조건과 카테고리에 맞는 게시글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getList(@RequestParam(value = "categoryId", required = true) String categoryId,
                                               @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/detail/{boardCode}")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getDetail(@PathVariable String boardCode) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 등록", description = "새로운 게시글을 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateBoardCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 수정", description = "게시글 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateBoardCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/restriction")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 제재", description = "관리자가 특정 게시글을 제재합니다.")
    public ResponseEntity<ResponseDto> restriction(@RequestBody @Valid RestrictCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{boardCode}")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 삭제", description = "게시글을 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String boardCode) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
