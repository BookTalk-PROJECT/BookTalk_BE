package com.booktalk_be.domain.gathering.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.JsonPrinter;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.command.CreateRecruitRequest;
import com.booktalk_be.domain.gathering.command.DeleteGatheringCommand;
import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringMemberMapRepository;
import com.booktalk_be.domain.gathering.responseDto.BookItemResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringEditInitResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.booktalk_be.domain.gathering.service.GatheringBookMapService;
import com.booktalk_be.domain.gathering.service.GatheringRecruitQuestionService;
import com.booktalk_be.domain.gathering.service.GatheringRecruitRequestService;
import com.booktalk_be.domain.gathering.service.GatheringService;
import com.booktalk_be.domain.member.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gathering")
@CrossOrigin("http://localhost:5173")
@Tag(name = "Gathering API", description = "모임 관련 API 입니다.")
public class GatheringController {

    private final GatheringService gatheringService;

    private final GatheringBookMapService gatheringBookMapService;

    private final GatheringRecruitQuestionService gatheringRecruitQuestionService;

    private final GatheringRecruitRequestService gatheringRecruitRequestService;


    @GetMapping("/list")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 조회", description = "조건에 맞는 모임을 페이징하여 조회합니다.")
    public ResponseEntity<ResponseDto> getList(
            @RequestParam(required = false) GatheringStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        System.out.println("상태 : "+ status + " 검색어 : "+search +" 페이지번호 : "+ page + " 사이즈 : "+size);
        Page<GatheringResponse> result = gatheringService.getList(status, search, page, size);
        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .msg("모임 목록 조회 성공")
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/detail/{code}")
    @Operation(summary = "모임 상세 조회", description = "code로 특정 모임의 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getDetail(
            @PathVariable String code,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        var result = gatheringService.getDetailByCode(code, member.getMemberId());

        System.out.println("멤버 아이디는 : "+member.getMemberId());

        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .msg("모임 상세 조회 성공")
                        .data(result)
                        .build()
        );
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 개설", description = "모임 정보를 포함한 이미지 파일을 업로드하여 모임을 개설합니다.")
    public ResponseEntity<ResponseDto> create(
            @RequestPart("data") @Valid CreateGatheringCommand requestData,
            @RequestPart(value = "image", required = false) MultipartFile imageFile, Authentication authentication) {
        try {
            Member member = (Member) authentication.getPrincipal();
            JsonPrinter.print(requestData);
            gatheringService.create(requestData, imageFile, member.getMemberId());

            return ResponseEntity.ok(
                    ResponseDto.builder()
                            .code(200)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseDto.builder()
                            .code(500)
                            .build()
            );
        }
    }

    @GetMapping("/{code}/books")
    @Operation(summary = "모임 책 목록", description = "gathering_code 기준으로 등록된 책 리스트를 반환합니다.")
    public ResponseEntity<ResponseDto> gatheringBookList(@PathVariable("code") String code) {
        var data = gatheringBookMapService.getBooksByGatheringCode(code);
        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .msg("책 목록 조회 성공")
                        .data(data) // Object 자리에 List<BookItemResponse> 들어감
                        .build()
        );
    }

    @GetMapping("/{code}")
    @Operation(summary = "모임 상세+편집 초기값 조회", description = "기존 상세 정보에 책/질문/해시태그를 합친 편집 초기 데이터를 반환합니다.")
    public ResponseEntity<GatheringEditInitResponse> getDetailForEdit(
            @PathVariable String code,
            Authentication authentication
    ) {
        int memberId = 0;
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Member m) {
            memberId = m.getMemberId();
        }
        return ResponseEntity.ok(gatheringService.getEditInitByCode(code, memberId));
    }

    @PatchMapping("/modify")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 수정", description = "모임을 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid CreateGatheringCommand requestData) {
        //gatheringService.create(requestData, member);

        return  ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/{code}/delete")
    @Operation(summary = "모임 소프트 삭제", description = "del_yn=1, del_reason 업데이트")
    public ResponseEntity<ResponseDto> softDelete(
            @PathVariable String code,
            @RequestBody @Valid DeleteGatheringCommand request,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        gatheringService.softDeleteGathering(code, request.getReason(), member);
        return ResponseEntity.ok(ResponseDto.builder().code(200).build());
    }

    @GetMapping("/recruitQuestionList")
    @Tag(name = "Gathering API")
    @Operation(summary = "모임 신청 질문 리스트 조회", description = "모임 신청 질문 리스트를 조회합니다.")
    public ResponseEntity<ResponseDto> getRecruitQuestionList(@RequestBody @Valid String gatheringId){
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/{code}/recruitQuestions")
    @Operation(summary = "모임 참여 질문 목록", description = "모임(gathering_code)에 연결된 참여 질문 리스트를 order 순으로 조회합니다.")
    public ResponseEntity<ResponseDto> gatheringRecruitList(@PathVariable("code") String code) {
        var data = gatheringRecruitQuestionService.getRecruitQuestions(code);
        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .msg("질문 목록 조회 성공")
                        .data(data)
                        .build()
        );
    }


    @PostMapping("/{code}/recruitRequest")
    @Operation(summary = "모임 가입 신청", description = "모임 참여 답변을 제출합니다.")
    public ResponseEntity<ResponseDto> gatheringRequestSubmit(
            @PathVariable("code") String code,
            @Valid @RequestBody RecruitRequestCommand command,
            Authentication authentication
    ) {
        Member memberId = (Member) authentication.getPrincipal();

        gatheringRecruitRequestService.submit(code, memberId, command);

        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .msg("가입 신청 완료")
                        .build()
        );
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
                                                    @RequestBody @Valid PostSearchCondCommand cmd){
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
