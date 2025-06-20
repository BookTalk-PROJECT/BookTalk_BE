package com.booktalk_be.domain.gathering.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.command.CreateRecruitRequest;
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

    @PatchMapping("/modify")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 수정", description = "모임을 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid CreateGatheringCommand requestData) {
        //gatheringService.create(requestData, member);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mypage/gatheringlist")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임", description = "마이 페이지의 내 모임들을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                          @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }


    @PatchMapping("/delete")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 삭제(비활성화)", description = "모임을 삭제(비활성화)합니다.")
    public ResponseEntity<ResponseDto> delete(@RequestBody @Valid String gatheringId) {
        //gatheringService.create(requestData, member);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mypage/boardlist")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 게시물 관리", description = "마이 페이지의 내 모임 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringBoardList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                               @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/recruitQuestionList")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 신청 질문 리스트 조회", description = "모임 신청 질문 리스트를 조회합니다.")
    public ResponseEntity<ResponseDto> getRecruitQuestionList(@RequestBody @Valid String gatheringId) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mypage/manage/request")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 신청 관리", description = "마이 페이지의 모임 신청 관리 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringRequestList
            (@RequestParam(value = "pageNum", required = true) Integer pageNum,
             @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/createRecruitRequest")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 신청 답변 등록", description = "모임 신청 질문 리스트를 작성하고 등록합니다.")
    public ResponseEntity<ResponseDto> createRecruitRequest(@RequestBody @Valid CreateRecruitRequest requestList) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/mypage/manage/approval")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 모임 승인 관리", description = "마이 페이지의 내 모임 승인 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringApprovalList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                                  @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/admin/boardlist")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 페이지 게시글 조회", description = "관리자 페이지의 게시글을 조회합니다.")
    public ResponseEntity<ResponseDto> getBoardList(@RequestParam(value = "category", required = true) String category,
                                                    @RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                    @RequestBody @Valid PostSearchCondCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/admin/restoration/{boardCode}")
    @Tag(name = "AdminPage API")
    @Operation(summary = "관리자 페이지 게시글 복구", description = "관리자 페이지의 게시글을 복구합니다.")
    public ResponseEntity<ResponseDto> restoreBoard(@RequestParam(value = "categoryId", required = true)
                                                    String categoryId,
                                                    @PathVariable String boardCode) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }
}
