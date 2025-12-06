package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.utils.JsonPrinter;
import com.booktalk_be.domain.gathering.command.QuestionCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionMap;
import com.booktalk_be.domain.gathering.model.entity.RecruitQuestion;
import com.booktalk_be.domain.gathering.model.repository.GatheringRecruitQuestionMapRepository;
import com.booktalk_be.domain.gathering.model.repository.RecruitQuestionRepository;
import com.booktalk_be.domain.gathering.responseDto.RecruitQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringRecruitQuestionServiceImpl implements GatheringRecruitQuestionService {

    private final GatheringRecruitQuestionMapRepository gatheringRecruitQuestionMapRepository;
    private final RecruitQuestionRepository recruitQuestionRepository;

    public void createRecruitQuestionMap(Gathering gatheringSaved, List<QuestionCommand> questions){
        //참여신청 질문 저장
        if (questions != null) {
            JsonPrinter.print(questions);
            List<RecruitQuestion> question = questions.stream()
                    .map(questionChild -> RecruitQuestion.builder()
                            .order(questionChild.getId())
                            .question(questionChild.getQuestion())
                            .build())
                    .toList();
            List<RecruitQuestion> questionsSaved = recruitQuestionRepository.saveAll(question);

            // 2. 매핑 테이블에 저장
            List<GatheringRecruitQuestionMap> questionMaps = questionsSaved.stream()
                    .map(q -> GatheringRecruitQuestionMap.builder()
                            .code(gatheringSaved)               // FK: Gathering
                            .recruitQuestion(q)                 // FK: RecruitQuestion
                            .build())
                    .toList();
            gatheringRecruitQuestionMapRepository.saveAll(questionMaps);
        }
    }

    @Override
    public List<RecruitQuestionResponse> getRecruitQuestions(String gatheringCode) {
        return gatheringRecruitQuestionMapRepository.findAllByGatheringCodeOrderByQuestionOrder(gatheringCode)
                .stream()
                .map(RecruitQuestionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatheringRecruitQuestionMap> findAllByGathering(Gathering gathering) {
        return gatheringRecruitQuestionMapRepository.findAllByCode(gathering);
    }


    @Override
    @Transactional
    public void syncRecruitQuestions(Gathering gathering, List<QuestionCommand> questions) {
        // 1) 기존 매핑 및 RecruitQuestion id들
        List<GatheringRecruitQuestionMap> existingMaps = gatheringRecruitQuestionMapRepository.findAllByCode(gathering);
        Map<Long, RecruitQuestion> existingQuestionsById = existingMaps.stream()
                .map(GatheringRecruitQuestionMap::getRecruitQuestion)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        RecruitQuestion::getRecruit_question,
                        rq -> rq,
                        (a, b) -> a
                ));

        // 2) 기존 매핑 통째로 제거
        if (!existingMaps.isEmpty()) {
            gatheringRecruitQuestionMapRepository.deleteAll(existingMaps);
        }

        // 3) 새로 들어온 리스트 기준으로 다시 구성
        if (questions == null || questions.isEmpty()) {
            // 질문을 전부 없애는 것도 허용. 여기서 그냥 끝내면 된다.
            return;
        }

        int order = 0;
        for (QuestionCommand qc : questions) {
            if (qc == null || !StringUtils.hasText(qc.getQuestion())) {
                continue;
            }

            Long id = qc.getId() > 0 ? (long) qc.getId() : null;

            RecruitQuestion rq;
            if (id != null) {
                // 기존 질문 재사용
                rq = existingQuestionsById.get(id);
                if (rq == null) {
                    // 다른 모임에서 썼다거나, DB가 꼬였을 가능성
                    rq = recruitQuestionRepository.findById(Math.toIntExact(id))
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 질문 ID: " + id));
                }
                rq.update(order, qc.getQuestion());
            } else {
                // 신규 질문
                rq = RecruitQuestion.builder()
                        .order(order)
                        .question(qc.getQuestion())
                        .build();
                rq = recruitQuestionRepository.save(rq);
            }

            // 새 매핑 생성
            GatheringRecruitQuestionMap map = GatheringRecruitQuestionMap.builder()
                    .code(gathering)
                    .recruitQuestion(rq)
                    .build();
            gatheringRecruitQuestionMapRepository.save(map);

            order++;
        }

        // RecruitQuestion 고아 정리는 지금은 안 한다.
        // 필요하면 나중에 "어디에도 매핑 안 된 질문은 삭제" 같은 배치로 정리 가능.
    }
}
