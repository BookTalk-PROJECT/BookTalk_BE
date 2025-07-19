package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.QuestionCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GatheringRecruitQuestionService {
    void createRecruitQuestionMap(Gathering gatheringSaved, List<QuestionCommand> questions);
}
