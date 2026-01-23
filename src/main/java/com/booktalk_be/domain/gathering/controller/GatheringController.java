package com.booktalk_be.domain.gathering.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.JsonPrinter;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.*;
import com.booktalk_be.domain.gathering.command.mypage.GatheringBoardSearchCondCommand;
import com.booktalk_be.domain.gathering.command.mypage.GatheringSearchCondCommand;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringMemberMapRepository;
import com.booktalk_be.domain.gathering.responseDto.*;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringBoardResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageGatheringResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitApprovalResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitRequestResponse;
import com.booktalk_be.domain.gathering.service.*;
import com.booktalk_be.domain.member.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gathering")
@Tag(name = "Gathering API", description = "모임 관련 API 입니다.")
public class GatheringController {

    private final GatheringService gatheringService;

    private final GatheringBookMapService gatheringBookMapService;

    private final GatheringRecruitQuestionService gatheringRecruitQuestionService;

    private final GatheringRecruitRequestService gatheringRecruitRequestService;

    private final GatheringBoardService gatheringBoardService;


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

    @PutMapping(value = "/modify/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "모임 수정", description = "생성 포맷과 동일한 JSON을 data 파트에 담아 multipart로 요청")
    public ResponseEntity<ResponseDto> update(
            @PathVariable String code,
            @RequestPart("data") @Valid EditGatheringRequest command,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {
        JsonPrinter.print(command);

        Member member = (Member) authentication.getPrincipal();
        gatheringService.updateGathering(code, command, image, member);

        return ResponseEntity.ok(
                ResponseDto.builder()
                        .code(200)
                        .build()
        );
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

    @PostMapping("/{code}/restore")
    @Operation(summary = "모임 소프트 삭제", description = "del_yn=0, del_reason 초기화")
    public ResponseEntity<ResponseDto> restore(
            @PathVariable String code,
            @RequestBody @Valid DeleteGatheringCommand request,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        gatheringService.restoreGathering(code, request.getReason(), member);
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
    //=====================================모임게시글=============================================
    //==========================================================================================


    @PostMapping("/board/create")
    @Operation(summary = "모임 게시글 등록", description = "모임 게시글을 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateGatheringBoardCommand cmd,
                                              Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        gatheringBoardService.create(cmd, member);
        return ResponseEntity.ok(ResponseDto.builder().code(200).build());
    }

    @PatchMapping("/board/modify")
    @Operation(summary = "모임 게시글 수정", description = "모임 게시글을 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateGatheringBoardCommand cmd) {
        gatheringBoardService.modify(cmd);
        return ResponseEntity.ok(ResponseDto.builder().code(200).build());
    }

    @DeleteMapping("/board/delete/{postCode}")
    @Operation(summary = "모임 게시글 삭제", description = "모임 게시글을 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String postCode) {
        gatheringBoardService.delete(postCode);
        return ResponseEntity.ok(ResponseDto.builder().code(200).build());
    }

    @GetMapping("/board/list/{gatheringCode}")
    @Operation(summary = "모임 게시글 목록", description = "모임별 게시글 목록을 페이징 조회합니다.")
    public ResponseEntity<ResponseDto> list(@PathVariable String gatheringCode,
                                            @RequestParam Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageResponseDto<GatheringBoardResponse> page = gatheringBoardService.list(gatheringCode, pageNum, pageSize);
        return ResponseEntity.ok(ResponseDto.builder().code(200).data(page).build());
    }

    @GetMapping("/board/detail/{postCode}")
    @Operation(summary = "모임 게시글 상세", description = "모임 게시글 상세 + 댓글을 조회합니다.")
    public ResponseEntity<ResponseDto> detail(@PathVariable String postCode) {
        System.out.println("올까용?");

        GatheringBoardDetailResponse res = gatheringBoardService.detail(postCode);
        return ResponseEntity.ok(ResponseDto.builder().code(200).data(res).build());
    }


    //========================================모임 마이페이지======================================
    //==========================================================================================


    //마이 페이지 내 모임 조회 API(All)
    @GetMapping("/myList")
    @Operation(summary = "마이페이지 내 모임 목록 조회", description = "내가 속한 모임 목록을 페이징 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatherings(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringResponse> page =
                gatheringService.getMyGatherings(pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    //마이 페이지 내 모임 조회 API(Search)
    @PostMapping("/myList/search")
    @Operation(summary = "마이페이지 내 모임 목록 검색", description = "검색어/날짜범위로 내 모임 목록을 필터링해 페이징 조회합니다. 값이 비면 조건 없이 전체 조회로 동작해야 합니다.")
    public ResponseEntity<ResponseDto> searchMyGatherings(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody @Valid GatheringSearchCondCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringResponse> page =
                gatheringService.searchMyGatherings(cmd, pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @GetMapping("/myBoardList")
    @Operation(summary = "마이페이지 모임 게시글 조회", description = "내가 작성한 모임 게시글을 페이징 조회합니다.")
    public ResponseEntity<ResponseDto> getMyGatheringBoards(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringBoardResponse> page =
                gatheringBoardService.getMyGatheringBoards(pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    @PostMapping("/myBoardList/search")
    @Operation(summary = "마이페이지 모임 게시글 검색", description = "검색어/작성일(reg_time) 범위로 내 모임 게시글을 검색합니다. 값이 비면 전체조회로 동작해야 합니다.")
    public ResponseEntity<ResponseDto> searchMyGatheringBoards(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestBody GatheringBoardSearchCondCommand cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageGatheringBoardResponse> page =
                gatheringBoardService.searchMyGatheringBoards(cmd, pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    //마이 페이지 내 모임 신청 신청 목록 조회 API
    @GetMapping("/myRecruitList")
    @Operation(summary = "마이페이지 모임 신청 조회", description = "내가 신청한 모임 목록(질문/답변 포함)을 페이징 조회합니다.")
    public ResponseEntity<ResponseDto> getMyRecruitRequests(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();

        PageResponseDto<MyPageRecruitRequestResponse> page =
                gatheringRecruitRequestService.getMyRecruitRequests(pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(page)
                .build());
    }

    //마이 페이지 모임 신청 목록 조회 API
    @GetMapping("/requestMyList")
    @Operation(summary = "모임장 신청 승인 목록", description = "내가 모임장인 모임에 들어온 참여 신청을 조회합니다.")
    public ResponseEntity<ResponseDto> list(
            @RequestParam(value = "pageNum", required = true) Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        PageResponseDto<MyPageRecruitApprovalResponse> page =
                gatheringRecruitRequestService.getApprovalList(pageNum, pageSize, member.getMemberId());

        return ResponseEntity.ok(ResponseDto.builder().code(200).data(page).build());
    }

    @PostMapping("/approve")
    public ResponseEntity<ResponseDto> approve(
            @RequestBody ApproveCmd cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        gatheringRecruitRequestService.approve(member.getMemberId(), cmd.getGathering_code(), cmd.getApplicant_id());
        return ResponseEntity.ok(ResponseDto.builder().code(200).data(true).build());
    }

    @PostMapping("/reject")
    public ResponseEntity<ResponseDto> reject(
            @RequestBody RejectCmd cmd,
            Authentication authentication
    ) {
        Member member = (Member) authentication.getPrincipal();
        gatheringRecruitRequestService.reject(member.getMemberId(), cmd.getGathering_code(), cmd.getApplicant_id(), cmd.getReject_reason());
        return ResponseEntity.ok(ResponseDto.builder().code(200).data(true).build());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveCmd {
        private String gathering_code;
        private int applicant_id;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class RejectCmd {
        private String gathering_code;
        private int applicant_id;
        private String reject_reason;
    }



}
