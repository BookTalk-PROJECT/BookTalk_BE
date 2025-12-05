package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.QuestionCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionMap;
import com.booktalk_be.domain.gathering.responseDto.RecruitQuestionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GatheringRecruitQuestionService {
    void createRecruitQuestionMap(Gathering gatheringSaved, List<QuestionCommand> questions);

    List<RecruitQuestionResponse> getRecruitQuestions(String gatheringCode);

    List<GatheringRecruitQuestionMap> findAllByGathering(Gathering gathering);

    void syncRecruitQuestions(Gathering gathering, List<QuestionCommand> questions);
}
