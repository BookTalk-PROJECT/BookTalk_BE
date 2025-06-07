package com.booktalk_be.domain.admin.controller;


import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "AdminPage API", description = "관리자 페이지 API 입니다.")
public class AdminController {

    //카테고리 컨트롤러 별개 존재하므로 따로 명시하지 않음

    //지금 화면 프론트로 구현되어 있기로는 전체 탭이 있는데 이런 경우 각 컨트롤러에서 api를 3번 요청해서 다 받아와야함, 비효율 적이라는 생각
    //통합 api를 사용하는게 맞지 않나 생각
    @GetMapping("/board")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 페이지 게시글 조회", description = "관리자 페이지의 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getBoardList(@RequestParam(value = "category", required = true) String category,
                                                              @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                              @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/board/restoration/{boardCode}")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 페이지 게시글 복구", description = "관리자 페이지의 게시글을 복구합니다.")
    public ResponseEntity<ResponseDto> restoreBoard(@RequestParam(value = "categoryId", required = true) String categoryId,
                                                    @PathVariable String boardCode)
                                             {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
