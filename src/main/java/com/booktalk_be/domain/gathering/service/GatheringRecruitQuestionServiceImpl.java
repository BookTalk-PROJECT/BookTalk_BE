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
import org.springframework.stereotype.Service;

import java.util.List;

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
}
