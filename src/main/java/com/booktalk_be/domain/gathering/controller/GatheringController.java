package com.booktalk_be.domain.gathering.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.booktalk_be.domain.gathering.service.GatheringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gathering")
@Tag(name = "Gathering API", description = "모임 관련 API 입니다.")
public class GatheringController {

    //private final GatheringService gatheringService;

    @GetMapping("/list")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 조회", description = "조건에 맞는 모임을 페이징하여 조회합니다.")
    public ResponseEntity<ResponseDto> getList(
            @RequestParam(required = false) GatheringStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        //Page<GatheringResponse> result = gatheringService.findGatherings(status, search, page, size);

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .msg("모임 목록 조회 성공")
                //.data(result) // 혹은 전체 Page 객체
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 개설", description = "모임을 개설합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateGatheringCommand requestData) {
        //gatheringService.create(requestData, member);

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //마이 페이지 내 모임 조회 API
    @GetMapping("/mylist")
    @Tag(name = "Gathering API")
    @Operation(summary = "내 모임 목록 조회", description = "내 모임 목록을 조회 합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                          @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //마이 페이지 모임 게시판 게시물 조회 API
    @GetMapping("/board/mylist")
    @Tag(name = "Gathering API")
    @Operation(summary = "내 모임 게시판 내 글 목록 조회", description = "내 모임 게시판 내 글 목록들을 조회 합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringBoardList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //마이 페이지 내 모임 신청 신청 목록 조회 API
    @GetMapping("/manage/request")
    @Tag(name = "Gathering API")
    @Operation(summary = "내 신청 모임 목록 조회", description = "내 신청 모임 목록들을 조회 합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringRequestList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                                 @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //마이 페이지 모임 신청 목록 조회 API
    @GetMapping("/manage/approval")
    @Tag(name = "Gathering API")
    @Operation(summary = "내 모임 승인 목록 조회", description = "내 모임에 신청한 신청 목록들을 조회 합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringApprovalList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                                  @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //관리자 페이지 모임 게시글 조회 API
    @GetMapping("/admin/boardlist")
    @Tag(name = "Gathering API")
    @Operation(summary = "관리자 게시글 조회", description = "관리자 권한으로 모든 게시글을 조회 합니다.")
    public ResponseEntity<ResponseDto> getBoardList(@RequestParam(value = "category", required = true) String category,
                                                    @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                    @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    //관리자 페이지 모임 게시글 복구 API
    @PostMapping("/admin/restoration/{boardCode}")
    @Tag(name = "Gathering API")
    @Operation(summary = "관리자 게시글 복구", description = "관리자 권한으로 게시글을 복구합니다.")
    public ResponseEntity<ResponseDto> restoreBoard(@RequestParam(value = "categoryId", required = true) String categoryId,
                                                    @PathVariable String boardCode)
    {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }
}
