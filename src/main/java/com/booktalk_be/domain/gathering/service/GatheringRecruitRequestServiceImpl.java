package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequest;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequestStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepository;
import com.booktalk_be.domain.gathering.model.repository.RecruitRequestRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
