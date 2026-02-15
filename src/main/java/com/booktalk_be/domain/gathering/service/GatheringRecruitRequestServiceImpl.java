package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequest;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequestStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepository;
import com.booktalk_be.domain.gathering.model.repository.RecruitRequestRepository;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitApprovalResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitRequestResponse;
import com.booktalk_be.domain.gathering.util.TypeConversionUtils;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringRecruitRequestServiceImpl implements GatheringRecruitRequestService{

    private final RecruitRequestRepository recruitRequestRepository;
    private final GatheringRepository gatheringRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void submit(String gatheringCode, Member memberId, RecruitRequestCommand command) {
        // 1) 레퍼런스 조회 (프록시)
        Gathering gatheringRef = gatheringRepository.getReferenceById(gatheringCode);
        Member memberRef = memberRepository.getReferenceById(memberId.getMemberId());


        // 3) 신규 엔티티들 빌드 → List로 모아 saveAll
        List<RecruitRequest> toSave = new ArrayList<>();
        for (RecruitRequestCommand.AnswerItem a : command.getAnswers()) {
            // recruitQuestion은 복합키의 String 필드이므로 문자열로 변환
            String recruitQuestionKey = String.valueOf(a.getQuestionId());

            RecruitRequest row = RecruitRequest.builder()
                    .code(gatheringRef)
                    .member(memberRef)
                    .recruitQuestion(recruitQuestionKey) // ★ 질문별 1행
                    .requestQuestionAnswer(a.getAnswer()) // 개별 답변을 여기에 저장(원치 않으면 null)
                    .status(RecruitRequestStatus.WAITING) // 기본값: 대기
                    .rejectReason(null)
                    .build();

            toSave.add(row);
        }

        // 4) 일괄 저장
        recruitRequestRepository.saveAll(toSave);
    }


    @Override
    public PageResponseDto<MyPageRecruitRequestResponse> getMyRecruitRequests(Integer pageNum, Integer pageSize, int memberId) {
        List<Object[]> rows = recruitRequestRepository.callMyRecruitRequestList(memberId, pageNum, pageSize);
        return buildPage(rows);
    }

    private PageResponseDto<MyPageRecruitRequestResponse> buildPage(List<Object[]> rows) {
        int totalPages = 0;
        if (rows != null && !rows.isEmpty()) {
            Object[] first = rows.get(0);
            totalPages = TypeConversionUtils.toInt(first[first.length - 1]); // 마지막 = total_pages
        }

        List<MyPageRecruitRequestResponse> content =
                (rows == null) ? List.of() : rows.stream().map(this::mapRow).toList();

        return PageResponseDto.<MyPageRecruitRequestResponse>builder()
                .content(content)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<MyPageRecruitApprovalResponse> getApprovalList(Integer pageNum, Integer pageSize, int masterId) {
        List<Object[]> rows = recruitRequestRepository.callRecruitApprovalList(masterId, pageNum, pageSize);
        return buildPages(rows);
    }

    @Override
    @Transactional
    public void approve(int masterId, String gatheringCode, int applicantId) {
        recruitRequestRepository.callRecruitApprove(masterId, gatheringCode, applicantId);
    }

    @Override
    @Transactional
    public void reject(int masterId, String gatheringCode, int applicantId, String rejectReason) {
        recruitRequestRepository.callRecruitReject(masterId, gatheringCode, applicantId, rejectReason);
    }

    @Override
    @Transactional
    public void withdraw(String gatheringCode, int memberId) {
        int deletedCount = recruitRequestRepository.withdrawRequest(gatheringCode, memberId);

        if (deletedCount == 0) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "철회할 수 있는 신청이 없습니다. (대기 중인 신청만 철회 가능)"
            );
        }
    }

    private PageResponseDto<MyPageRecruitApprovalResponse> buildPages(List<Object[]> rows) {
        int totalPages = 0;
        if (rows != null && !rows.isEmpty()) {
            Object[] first = rows.get(0);
            totalPages = TypeConversionUtils.toInt(first[first.length - 1]); // 마지막 = total_pages
        }

        List<MyPageRecruitApprovalResponse> content =
                (rows == null) ? List.of() : rows.stream().map(this::mapRows).toList();

        return PageResponseDto.<MyPageRecruitApprovalResponse>builder()
                .content(content)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 프로시저 컬럼 순서(권장):
     * 0 gathering_code
     * 1 gathering_name
     * 2 qa_json
     * 3 status
     * 4 reject_reason
     * 5 total_pages (마지막)
     */
    private MyPageRecruitRequestResponse mapRow(Object[] r) {
        return MyPageRecruitRequestResponse.builder()
                .gatheringCode(r[0] == null ? null : String.valueOf(r[0]))
                .gatheringName(r[1] == null ? null : String.valueOf(r[1]))
                .qaJson(r[2] == null ? "[]" : String.valueOf(r[2]))
                .status(r[3] == null ? null : String.valueOf(r[3]))
                .rejectReason(r[4] == null ? null : String.valueOf(r[4]))
                .build();
    }

    private MyPageRecruitApprovalResponse mapRows(Object[] r) {
        return MyPageRecruitApprovalResponse.builder()
                .gatheringCode(r[0] == null ? null : String.valueOf(r[0]))
                .gatheringName(r[1] == null ? null : String.valueOf(r[1]))
                .applicantId(TypeConversionUtils.toInt(r[2]))
                .applicantName(r[3] == null ? null : String.valueOf(r[3]))
                .qaJson(r[4] == null ? "[]" : String.valueOf(r[4]))
                .status(r[5] == null ? null : String.valueOf(r[5]))
                .rejectReason(r[6] == null ? null : String.valueOf(r[6]))
                .build();
    }
}
