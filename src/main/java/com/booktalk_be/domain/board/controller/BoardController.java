package com.booktalk_be.domain.board.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.board.command.CreateBoardCommand;
import com.booktalk_be.common.command.RestrictCommand;
import com.booktalk_be.domain.board.command.UpdateBoardCommand;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.board.service.BoardService;
import com.booktalk_be.domain.member.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/board")
@RequiredArgsConstructor
@Tag(name = "Community Board API", description = "커뮤니티 게시판 API 입니다.")
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/list")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 목록 조회", description = "카테고리에 맞는 게시글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getList(@RequestParam(value = "categoryId", required = true) Integer categoryId,
                                               @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        PageResponseDto<BoardResponse> page =  boardService.getBoardsForPaging(categoryId, pageNum, pageSize);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/list/search")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 목록 검색", description = "검색 조건과 카테고리에 맞는 게시글 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getList(@RequestParam(value = "categoryId", required = true) Integer categoryId,
                                               @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        PageResponseDto<BoardResponse> page =  boardService.searchBoardsForPaging(categoryId, pageNum, pageSize, cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @GetMapping("/detail/{boardCode}")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getDetail(@PathVariable String boardCode) {
        BoardDetailResponse res = boardService.getBoardDetail(boardCode);
        if(res.getPost().getDelYn()) {
            return new ResponseEntity<>(ResponseDto.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .data("삭제된 게시글 입니다.")
                    .build(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(res)
                .build());
    }

    @GetMapping("/query/next")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 다음 글 코드 조회", description = "다음 게시글의 글 코드를 조회합니다.")
    public ResponseEntity<ResponseDto> queryNextBoard(
            @RequestParam String boardCode,
            @RequestParam Integer categoryId
            ) {
        String res = boardService.queryNextBoard(boardCode, categoryId);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(res)
                .build());
    }

    @GetMapping("/query/prev")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 이전 글 코드 조회", description = "이전 게시글의 글 코드를 조회합니다.")
    public ResponseEntity<ResponseDto> queryPrevBoard(
            @RequestParam String boardCode,
            @RequestParam Integer categoryId
    ) {
        String res = boardService.queryPrevBoard(boardCode, categoryId);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(res)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 등록", description = "새로운 게시글을 등록합니다.")
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid CreateBoardCommand cmd,
            Authentication authentication
    ) {
        Member member =  (Member) authentication.getPrincipal();
        boardService.createBoard(cmd, member);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 수정", description = "게시글 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateBoardCommand cmd) {
        boardService.modifyBoard(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/restrict")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 제재", description = "관리자가 특정 게시글을 제재합니다.")
    public ResponseEntity<ResponseDto> restriction(@RequestBody @Valid RestrictCommand cmd) {
        boardService.restrictBoard(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/recover/{boardCode}")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 제재", description = "관리자가 특정 게시글을 제재합니다.")
    public ResponseEntity<ResponseDto> recover(@PathVariable String boardCode) {
        boardService.recoverBoard(boardCode);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{boardCode}")
    @Tag(name = "Community Board API")
    @Operation(summary = "커뮤니티 게시글 삭제", description = "게시글을 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String boardCode) {
        boardService.deleteBoard(boardCode);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/admin/all")
    @Tag(name = "Admin Page All Community Board API")
    @Operation(summary = "관리자 페이지 커뮤니티 게시글 전체 조회", description = "관리자 페이지의 모든 커뮤니티 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getAdminPageAll(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                       @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        PageResponseDto<BoardResponse> page =  boardService.getAllBoardsForPaging(pageNum, pageSize);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/admin/search")
    @Tag(name = "Community Board API")
    @Operation(summary = "관리자 페이지 커뮤니티 게시글 검색", description = "관리자 페이지의 모든 커뮤니티 게시글을 검색합니다.")
    public ResponseEntity<ResponseDto> searchAdminPage(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody @Valid PostSearchCondCommand cmd,
            Authentication authentication
    ) {
        Member member =  (Member) authentication.getPrincipal();
        PageResponseDto<BoardResponse> page =  boardService.searchAllBoardsForPaging(cmd, pageNum, pageSize
        );
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    //마이 페이지 내 커뮤니티 게시글 조회 API
    @GetMapping("/mylist")
    @Tag(name = "Community Board API")
    @Operation(summary = "마이 페이지 커뮤니티 게시글 조회", description = "마이 페이지의 커뮤니티 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getCommunityBoardList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member =  (Member) authentication.getPrincipal();
        PageResponseDto<BoardResponse> page =  boardService.getAllBoardsForPagingByMe(pageNum, pageSize, member.getMemberId());
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }
    @PostMapping("/mylist/search")
    @Tag(name = "Community Board API")
    @Operation(summary = "마이 페이지 커뮤니티 게시글 검색", description = "마이 페이지의 커뮤니티 게시글을 검색합니다.")
    public ResponseEntity<ResponseDto> searchCommunityBoardList(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody @Valid PostSearchCondCommand cmd,
            Authentication authentication
    ) {
        Member member =  (Member) authentication.getPrincipal();
        PageResponseDto<BoardResponse> page =  boardService.searchAllBoardsForPagingByMe(cmd, pageNum, pageSize, member.getMemberId());
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

}
